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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
    public RecoveryResult requestResend() {

        final PartyInfo partyInfo = partyInfoService.getPartyInfo();
        final Set<Party> parties = partyInfo.getParties();

        final long failures =
                parties.stream()
                        .filter(p -> !p.getUrl().equals(partyInfo.getUrl()))
                        .filter(p -> !transactionRequester.requestAllTransactionsFromNode(p.getUrl()))
                        .count();

        if (failures == 0) {
            return RecoveryResult.SUCCESS;
        } else {
            if (failures == parties.size()) {
                return RecoveryResult.FAILURE;
            }
        }

        return RecoveryResult.PARTIAL_SUCCESS;
    }

    @Override
    public RecoveryResult stage() {

        final AtomicLong stage = new AtomicLong(0);

        while(stagingEntityDAO.updateStageForBatch(BATCH_SIZE,stage.incrementAndGet()) != 0) {
        }

        final long totalCount = stagingEntityDAO.countAll();
        final long countValidated = stagingEntityDAO.countStaged();

        if (countValidated == totalCount) {
            return RecoveryResult.SUCCESS;
        }

        if (countValidated == 0) {
            return RecoveryResult.FAILURE;
        }

        return RecoveryResult.PARTIAL_SUCCESS;
    }

    @Override
    public RecoveryResult sync() {
        int payloadCount = 0;
        final AtomicInteger syncFailureCount = new AtomicInteger(0);

        int offset = 0;
        final int maxResult = BATCH_SIZE;

        while (offset < stagingEntityDAO.countAll()) {
            final List<StagingTransaction> transactions =
                stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(offset, maxResult);

            Map<String,List<StagingTransaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(StagingTransaction::getHash));


            payloadCount = transactions.size();

//                if (Objects.nonNull(transaction.getIssues())) {
//                    LOGGER.warn(
//                            "There are data consistency issue across versions of this staging transaction."
//                                    + "Please check for a potential malicious attempt during recovery. "
//                                    + "This staging transaction will be ignored");
//                    syncFailureCount += payloadsToSend.size();
//                    continue;
//                }

            grouped.entrySet().forEach(e -> {
                e.getValue().forEach(t -> {
                    PrivacyMode privacyMode = t.getPrivacyMode();
                    byte[] payload = t.getPayload();
                    try {
                        transactionManager.storePayload(payload);
                        if(privacyMode != PrivacyMode.STANDARD_PRIVATE) {
                            return;
                        }
                    } catch (PrivacyViolationException | StoreEntityException ex) {
                        LOGGER.error("An error occured during batch resend sync stage.", ex);
                        syncFailureCount.incrementAndGet();
                    }
                });
            });


            offset += maxResult;
        }

        if (syncFailureCount.get() > 0) {
            LOGGER.warn(
                "There have been issues during the synchronisation process. "
                    + "Problematic transactions have been ignored.");
        }

        if (syncFailureCount.get() == 0) {
            return RecoveryResult.SUCCESS;
        }

        if (syncFailureCount.get() == payloadCount) {
            return RecoveryResult.FAILURE;
        }

        return RecoveryResult.PARTIAL_SUCCESS;
    }
}
