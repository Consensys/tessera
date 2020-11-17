package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendResponse;
import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class LegacyResendManagerImpl implements LegacyResendManager {

    private final Enclave enclave;

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final int resendFetchSize;

    private final PayloadEncoder payloadEncoder;

    private final PayloadPublisher payloadPublisher;

    private final Discovery discovery;

    public LegacyResendManagerImpl(final Enclave enclave,
                                   final EncryptedTransactionDAO encryptedTransactionDAO,
                                   final int resendFetchSize,
                                   final PayloadEncoder payloadEncoder,
                                   final PayloadPublisher payloadPublisher,
                                   final Discovery discovery) {
        this.enclave = Objects.requireNonNull(enclave);
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.resendFetchSize = resendFetchSize;
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.payloadPublisher = Objects.requireNonNull(payloadPublisher);
        this.discovery = Objects.requireNonNull(discovery);
    }

    @Override
    public ResendResponse resend(ResendRequest request) {
        if (request.getType() == ResendRequest.ResendRequestType.INDIVIDUAL) {
            return resendIndividual(request.getRecipient(), request.getHash());
        }

        final LegacyWorkflowFactory batchWorkflowFactory
            = new LegacyWorkflowFactory(enclave, payloadEncoder, discovery, payloadPublisher);

        final BatchWorkflow batchWorkflow = batchWorkflowFactory.create();

        final long transactionCount = encryptedTransactionDAO.transactionCount();
        final long batchCount = calculateBatchCount(resendFetchSize, transactionCount);

        IntStream.range(0, (int) batchCount)
            .map(i -> i * resendFetchSize)
            .mapToObj(offset -> encryptedTransactionDAO.retrieveTransactions(offset, resendFetchSize))
            .flatMap(List::stream)
            .forEach(
                encryptedTransaction -> {
                    final BatchWorkflowContext context = new BatchWorkflowContext();
                    context.setEncryptedTransaction(encryptedTransaction);
                    context.setRecipientKey(request.getRecipient());
                    context.setBatchSize(1);
                    batchWorkflow.execute(context);
                });

        return ResendResponse.Builder.create().build();
    }

    protected ResendResponse resendIndividual(final PublicKey targetResendKey, final MessageHash messageHash) {
        final EncryptedTransaction encryptedTransaction =
            encryptedTransactionDAO
                .retrieveByHash(messageHash)
                .orElseThrow(
                    () -> new TransactionNotFoundException("Message with hash " + messageHash + " was not found")
                );

        final EncodedPayload payload = payloadEncoder.decode(encryptedTransaction.getEncodedPayload());

        if (payload.getPrivacyMode() != PrivacyMode.STANDARD_PRIVATE) {
            throw new EnhancedPrivacyNotSupportedException("Cannot resend enhanced privacy transaction in legacy resend");
        }

        if (!Objects.equals(payload.getSenderKey(), targetResendKey)) {
            final EncodedPayload formattedPayload = payloadEncoder.forRecipient(payload, targetResendKey);
            return ResendResponse.Builder.create().withPayload(formattedPayload).build();
        }

        final BatchWorkflowContext context = new BatchWorkflowContext();
        context.setEncodedPayload(payload);
        context.setEncryptedTransaction(encryptedTransaction);

        new SearchRecipientKeyForPayload(enclave).execute(context);
        final EncodedPayload formattedPayload = context.getEncodedPayload();
        return ResendResponse.Builder.create().withPayload(formattedPayload).build();
    }

    static int calculateBatchCount(long maxResults, long total) {
        return (int) Math.ceil((double) total / maxResults);
    }
}
