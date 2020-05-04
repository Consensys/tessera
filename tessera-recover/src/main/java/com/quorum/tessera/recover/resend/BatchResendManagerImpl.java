package com.quorum.tessera.recover.resend;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.data.staging.StagingTransactionConverter;
import com.quorum.tessera.data.staging.StagingTransactionVersion;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.service.Service;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import com.quorum.tessera.transaction.exception.StoreEntityException;
import com.quorum.tessera.util.Base64Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class BatchResendManagerImpl implements BatchResendManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResendManagerImpl.class);

    private static final int BATCH_SIZE = 10000;

    private final PayloadEncoder payloadEncoder;

    private final Base64Decoder base64Decoder;

    private final Enclave enclave;

    private final TransactionManager resendStoreDelegate;

    private final StagingEntityDAO stagingEntityDAO;

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PartyInfoService partyInfoService;

    public BatchResendManagerImpl(
            Enclave enclave,
            TransactionManager transactionManager,
            StagingEntityDAO stagingEntityDAO,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PartyInfoService partyInfoService) {
        this(
                PayloadEncoder.create(),
                Base64Decoder.create(),
                enclave,
                transactionManager,
                stagingEntityDAO,
                encryptedTransactionDAO,
                partyInfoService);
    }

    public BatchResendManagerImpl(
            PayloadEncoder payloadEncoder,
            Base64Decoder base64Decoder,
            Enclave enclave,
            TransactionManager transactionManager,
            StagingEntityDAO stagingEntityDAO,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PartyInfoService partyInfoService) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.base64Decoder = Objects.requireNonNull(base64Decoder);
        this.enclave = Objects.requireNonNull(enclave);
        this.resendStoreDelegate = Objects.requireNonNull(transactionManager);
        this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @Override
    @Transactional
    public ResendBatchResponse resendBatch(ResendBatchRequest request) {

        validateEnclaveStatus();

        final int batchSize = request.getBatchSize();
        final byte[] publicKeyData = base64Decoder.decode(request.getPublicKey());
        final PublicKey recipientPublicKey = PublicKey.from(publicKeyData);
        final AtomicLong messageCount = new AtomicLong(0);
        final List<EncodedPayload> batch = new ArrayList<>(batchSize);

        int offset = 0;
        final int maxResult = 10000;

        while (offset < encryptedTransactionDAO.transactionCount()) {
            // TODO this loop needs to be refactored to only pull the relevant records from DB (when
            // EncryptedTransaction is normalized).
            encryptedTransactionDAO.retrieveTransactions(offset, maxResult).stream()
                    .map(EncryptedTransaction::getEncodedPayload)
                    .map(payloadEncoder::decode)
                    .filter(
                            payload -> {
                                final boolean isCurrentNodeSender =
                                        payload.getRecipientKeys().contains(recipientPublicKey)
                                                && enclave.getPublicKeys().contains(payload.getSenderKey());
                                final boolean isRequestedNodeSender =
                                        Objects.equals(payload.getSenderKey(), recipientPublicKey);
                                return isCurrentNodeSender || isRequestedNodeSender;
                            })
                    .forEach(
                            payload -> {
                                EncodedPayload prunedPayload;

                                if (Objects.equals(payload.getSenderKey(), recipientPublicKey)) {
                                    prunedPayload = payload;
                                    if (payload.getRecipientKeys().isEmpty()) {
                                        // TODO Should we stop the whole resend just because we could not find a key
                                        // for a tx? Log instead?
                                        // a malicious party may be able to craft TXs that prevent others from
                                        // performing resends
                                        final PublicKey decryptedKey =
                                                searchForRecipientKey(payload)
                                                        .orElseThrow(
                                                                () -> {
                                                                    final MessageHash hash =
                                                                            MessageHashFactory.create()
                                                                                    .createFromCipherText(
                                                                                            payload.getCipherText());
                                                                    return new RecipientKeyNotFoundException(
                                                                            "No key found as recipient of message "
                                                                                    + hash);
                                                                });

                                        prunedPayload = payloadEncoder.withRecipient(payload, decryptedKey);
                                    }
                                } else {
                                    prunedPayload = payloadEncoder.forRecipient(payload, recipientPublicKey);
                                }

                                batch.add(prunedPayload);
                                messageCount.incrementAndGet();

                                if (batch.size() == batchSize) {
                                    partyInfoService.publishBatch(batch, recipientPublicKey);
                                    batch.clear();
                                }
                            });
            offset += maxResult;
        }

        if (batch.size() > 0) {
            partyInfoService.publishBatch(batch, recipientPublicKey);
        }

        return new ResendBatchResponse(messageCount.get());
    }

    // TODO use some locking mechanism to make this more efficient
    @Override
    @Transactional
    public synchronized void storeResendBatch(PushBatchRequest resendPushBatchRequest) {
        resendPushBatchRequest.getEncodedPayloads().stream()
                .map(StagingTransactionConverter::fromRawPayload)
                .forEach(this::handleStagingTransaction);
    }

    @Override
    public Result performStaging() {
        stagingEntityDAO.performStaging(BATCH_SIZE);

        final long totalCount = stagingEntityDAO.countAll();
        final long countValidated = stagingEntityDAO.countStaged();

        if (countValidated == totalCount) {
            return Result.SUCCESS;
        }

        if (countValidated == 0) {
            return Result.FAILURE;
        }

        return Result.PARTIAL_SUCCESS;
    }

    @Override
    public Result performSync() {

        int payloadCount = 0;
        int syncFailureCount = 0;

        int offset = 0;
        final int maxResult = BATCH_SIZE;

        while (offset < stagingEntityDAO.countAll()) {
            final List<StagingTransaction> transactions =
                    stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(offset, maxResult);

            for (StagingTransaction transaction : transactions) {
                final List<byte[]> payloadsToSend =
                        transaction.getVersions().stream()
                                .map(StagingTransactionVersion::getPayload)
                                .collect(Collectors.toList());
                payloadCount += payloadsToSend.size();

                if (Objects.nonNull(transaction.getIssues())) {
                    LOGGER.warn(
                            "There are data consistency issue across versions of this staging transaction."
                                    + "Please check for a potential malicious attempt during recovery. "
                                    + "This staging transaction will be ignored");
                    syncFailureCount += payloadsToSend.size();
                    continue;
                }

                for (byte[] payload : payloadsToSend) {
                    try {
                        resendStoreDelegate.storePayload(payload);
                    } catch (PrivacyViolationException | StoreEntityException ex) {
                        LOGGER.error("An error occured during batch resend sync stage.", ex);
                        syncFailureCount++;
                    }
                }
            }
            offset += maxResult;
        }

        if (syncFailureCount > 0) {
            LOGGER.warn(
                    "There have been issues during the synchronisation process. "
                            + "Problematic transactions have been ignored.");
        }

        if (syncFailureCount == 0) {
            return Result.SUCCESS;
        }

        if (syncFailureCount == payloadCount) {
            return Result.FAILURE;
        }

        return Result.PARTIAL_SUCCESS;
    }


    @Override
    public boolean isResendMode() {
        return true;
    }

    private void handleStagingTransaction(StagingTransaction stagingTransaction) {
        final Optional<StagingTransaction> existingTransaction =
                stagingEntityDAO.retrieveByHash(stagingTransaction.getHash());
        if (existingTransaction.isPresent()) {
            final StagingTransaction merged =
                    StagingTransactionConverter.versionStagingTransaction(
                            existingTransaction.get(), stagingTransaction);
            stagingEntityDAO.update(merged);
        } else {
            stagingEntityDAO.save(stagingTransaction);
        }
    }

    private void validateEnclaveStatus() {
        if (enclave.status() == Service.Status.STOPPED) {
            throw new EnclaveNotAvailableException();
        }
    }

    private Optional<PublicKey> searchForRecipientKey(final EncodedPayload payload) {
        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                enclave.unencryptTransaction(payload, potentialMatchingKey);
                return Optional.of(potentialMatchingKey);
            } catch (EnclaveNotAvailableException | IndexOutOfBoundsException | EncryptorException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }
}
