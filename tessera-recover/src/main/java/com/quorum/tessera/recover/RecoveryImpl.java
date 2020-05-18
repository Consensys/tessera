package com.quorum.tessera.recover;

import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.sync.TransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.transaction.exception.StoreEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class RecoveryImpl implements Recovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryImpl.class);

    private static final int BATCH_SIZE = 10000;

    private final StagingEntityDAO stagingEntityDAO;

    private final PartyInfoService partyInfoService;

    private final TransactionRequester transactionRequester;

    private final TransactionManager transactionManager;

    public RecoveryImpl(
        StagingEntityDAO stagingEntityDAO,
        PartyInfoService partyInfoService,
        TransactionRequester transactionRequester,
        TransactionManager transactionManager) {
        this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.transactionRequester = Objects.requireNonNull(transactionRequester);
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @Override
    public RecoveryResult request() {

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();
        final Set<Party> parties = partyInfo.getParties();

        final long failures =
            parties.stream()
                .filter(p -> !p.getUrl().equals(partyInfo.getUrl()))
                .filter(p -> !transactionRequester.requestAllTransactionsFromNode(p.getUrl()))
                .count();

        if (failures > 0) {
            if (failures == parties.size()) {
                return RecoveryResult.FAILURE;
            }
            return RecoveryResult.PARTIAL_SUCCESS;
        }
        return RecoveryResult.SUCCESS;
    }

    @Override
    public RecoveryResult stage() {

        final AtomicLong stage = new AtomicLong(0);

        while (stagingEntityDAO.updateStageForBatch(BATCH_SIZE, stage.incrementAndGet()) != 0) {
        }

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

        int offset = 0;
        final int maxResult = BATCH_SIZE;

        while (offset < stagingEntityDAO.countAll()) {

            final List<StagingTransaction> transactions =
                stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(offset, maxResult);

            final Map<String, List<StagingTransaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(StagingTransaction::getHash, LinkedHashMap::new, toList()));

            grouped.forEach(
                (key, value) -> value.stream()
                    .filter(
                        t -> {
                            payloadCount.incrementAndGet();
                            byte[] payload = t.getPayload();
                            try {
                                transactionManager.storePayload(payload);
                            } catch (PrivacyViolationException | StoreEntityException | IllegalArgumentException ex) {
                                LOGGER.error("An error occurred during batch resend sync stage.", ex);
                                syncFailureCount.incrementAndGet();
                            }
                            return PrivacyMode.PRIVATE_STATE_VALIDATION == t.getPrivacyMode();
                        })
                    .findFirst());

            offset += maxResult;
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
}
