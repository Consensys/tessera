package com.quorum.tessera.recovery.internal;

import static java.util.stream.Collectors.toList;

import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.recovery.RecoveryResult;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.version.EnhancedPrivacyVersion;
import jakarta.persistence.PersistenceException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RecoveryImpl implements Recovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryImpl.class);

  private static final int BATCH_SIZE = 10000;

  private final StagingEntityDAO stagingEntityDAO;

  private final Discovery discovery;

  private final BatchTransactionRequester transactionRequester;

  private final TransactionManager transactionManager;

  RecoveryImpl(
      StagingEntityDAO stagingEntityDAO,
      Discovery discovery,
      BatchTransactionRequester transactionRequester,
      TransactionManager transactionManager) {
    this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
    this.discovery = Objects.requireNonNull(discovery);
    this.transactionRequester = Objects.requireNonNull(transactionRequester);
    this.transactionManager = Objects.requireNonNull(transactionManager);
  }

  @Override
  public RecoveryResult request() {

    final Set<NodeInfo> remoteNodeInfos = discovery.getRemoteNodeInfos();

    final Predicate<NodeInfo> sendRequestsToNode =
        nodeInfo ->
            nodeInfo.supportedApiVersions().contains(EnhancedPrivacyVersion.API_VERSION_2)
                && transactionRequester.requestAllTransactionsFromNode(nodeInfo.getUrl());

    final Predicate<NodeInfo> sendRequestsToLegacyNode =
        nodeInfo ->
            !nodeInfo.supportedApiVersions().contains(EnhancedPrivacyVersion.API_VERSION_2)
                && transactionRequester.requestAllTransactionsFromLegacyNode(nodeInfo.getUrl());

    final long failures =
        remoteNodeInfos.stream()
            .filter(sendRequestsToNode.or(sendRequestsToLegacyNode).negate())
            .peek(p -> LOGGER.warn("Fail resend request to {}", p.getUrl()))
            .count();

    if (failures > 0) {
      if (failures == remoteNodeInfos.size()) {
        return RecoveryResult.FAILURE;
      }
      return RecoveryResult.PARTIAL_SUCCESS;
    }
    return RecoveryResult.SUCCESS;
  }

  @Override
  public RecoveryResult stage() {

    final AtomicLong stage = new AtomicLong(0);

    while (stagingEntityDAO.updateStageForBatch(BATCH_SIZE, stage.incrementAndGet()) != 0) {}

    final long totalCount = stagingEntityDAO.countAll();
    final long validatedCount = stagingEntityDAO.countStaged();

    if (validatedCount < totalCount) {
      if (validatedCount == 0) {
        return RecoveryResult.FAILURE;
      }
      return RecoveryResult.PARTIAL_SUCCESS;
    }
    return RecoveryResult.SUCCESS;
  }

  @Override
  public RecoveryResult sync() {

    final AtomicInteger payloadCount = new AtomicInteger(0);
    final AtomicInteger syncFailureCount = new AtomicInteger(0);

    final int maxResult = BATCH_SIZE;

    for (int offset = 0; offset < stagingEntityDAO.countAll(); offset += maxResult) {

      final List<StagingTransaction> transactions =
          stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(offset, maxResult);

      final Map<String, List<StagingTransaction>> grouped =
          transactions.stream()
              .collect(
                  Collectors.groupingBy(StagingTransaction::getHash, LinkedHashMap::new, toList()));

      grouped.forEach(
          (key, value) ->
              value.stream()
                  .filter(
                      t -> {
                        payloadCount.incrementAndGet();
                        EncodedPayload encodedPayload = t.getEncodedPayload();
                        try {
                          transactionManager.storePayload(encodedPayload);
                        } catch (PrivacyViolationException | PersistenceException ex) {
                          LOGGER.error("An error occurred during batch resend sync stage.", ex);
                          syncFailureCount.incrementAndGet();
                        }
                        return PrivacyMode.PRIVATE_STATE_VALIDATION == t.getPrivacyMode();
                      })
                  .findFirst());
    }

    if (syncFailureCount.get() > 0) {
      LOGGER.warn(
          "There have been issues during the synchronisation process. "
              + "Problematic transactions have been ignored.");
      if (syncFailureCount.get() == payloadCount.get()) {
        return RecoveryResult.FAILURE;
      }
      return RecoveryResult.PARTIAL_SUCCESS;
    }
    return RecoveryResult.SUCCESS;
  }

  @Override
  public int recover() {

    try {
      if (stagingEntityDAO.countAll() != 0 || stagingEntityDAO.countAllAffected() != 0) {
        LOGGER.error(
            "Staging tables are not empty. Please ensure database has been setup correctly for recovery process");
        return RecoveryResult.FAILURE.getCode();
      }
    } catch (Exception ex) {
      LOGGER.error(
          "Attempt to query failed. Please ensure database has been setup correctly for recovery process");
      return RecoveryResult.FAILURE.getCode();
    }

    final long startTime = System.nanoTime();

    LOGGER.debug("Requesting transactions from other nodes");
    final RecoveryResult resendResult = request();

    final long resendFinished = System.nanoTime();

    LOGGER.debug("Perform staging of transactions");
    final RecoveryResult stageResult = stage();

    final long stagingFinished = System.nanoTime();

    LOGGER.debug("Perform synchronisation of transactions");
    final RecoveryResult syncResult = sync();

    final long syncFinished = System.nanoTime();

    LOGGER.info(
        "Resend Stage: {} (duration = {} ms). Staging Stage: {} (duration = {} ms). Sync Stage: {} (duration = {} ms)",
        resendResult,
        (resendFinished - startTime) / 1000000,
        stageResult,
        (stagingFinished - resendFinished) / 1000000,
        syncResult,
        (syncFinished - stagingFinished) / 1000000);

    final long endTime = System.nanoTime();
    LOGGER.info("Recovery process took {} ms", (endTime - startTime) / 1000000);

    return Stream.of(resendResult, stageResult, syncResult)
        .map(RecoveryResult::getCode)
        .reduce(Integer::max)
        .get();
  }
}
