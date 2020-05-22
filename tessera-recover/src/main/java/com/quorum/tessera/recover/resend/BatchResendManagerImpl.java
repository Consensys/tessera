package com.quorum.tessera.recover.resend;


import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransactionUtils;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.*;
import com.quorum.tessera.util.Base64Codec;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class BatchResendManagerImpl implements BatchResendManager {


    private final PayloadEncoder payloadEncoder;

    private final Base64Codec base64Decoder;

    private final Enclave enclave;

    private final StagingEntityDAO stagingEntityDAO;

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PartyInfoService partyInfoService;

    private final ResendBatchPublisher resendBatchPublisher;

    private final int maxResults;

    public BatchResendManagerImpl(
        Enclave enclave,
        StagingEntityDAO stagingEntityDAO,
        EncryptedTransactionDAO encryptedTransactionDAO,
        PartyInfoService partyInfoService,ResendBatchPublisher resendBatchPublisher,int maxResults) {
        this(
            PayloadEncoder.create(),
            Base64Codec.create(),
            enclave,
            stagingEntityDAO,
            encryptedTransactionDAO,
            partyInfoService,
            resendBatchPublisher,maxResults);
    }

    public BatchResendManagerImpl(
        PayloadEncoder payloadEncoder,
        Base64Codec base64Decoder,
        Enclave enclave,
        StagingEntityDAO stagingEntityDAO,
        EncryptedTransactionDAO encryptedTransactionDAO,
        PartyInfoService partyInfoService,ResendBatchPublisher resendBatchPublisher,int maxResults) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.base64Decoder = Objects.requireNonNull(base64Decoder);
        this.enclave = Objects.requireNonNull(enclave);
        this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.resendBatchPublisher = Objects.requireNonNull(resendBatchPublisher);
        this.maxResults = maxResults;


    }

    static int calculateBatchCount(long batchSize,long total) {
        return (int) Math.ceil((double)total / batchSize);
    }

    @Override
    public ResendBatchResponse resendBatch(ResendBatchRequest request) {

        final int batchSize = request.getBatchSize();
        final byte[] publicKeyData = base64Decoder.decode(request.getPublicKey());
        final PublicKey recipientPublicKey = PublicKey.from(publicKeyData);

        final long transactionCount = encryptedTransactionDAO.transactionCount();
        final long batchCount = calculateBatchCount(maxResults,transactionCount);

        final BatchWorkflow batchWorkflow = BatchWorkflowFactory.newFactory(enclave,payloadEncoder,partyInfoService,resendBatchPublisher).create();

        IntStream.range(0, (int) batchCount)
            .map(i -> i * maxResults)
            .mapToObj(offset -> encryptedTransactionDAO.retrieveTransactions(offset, maxResults))
            .flatMap(List::stream)
            .forEach(encryptedTransaction -> {
                final BatchWorkflowContext context = new BatchWorkflowContext();
                context.setEncryptedTransaction(encryptedTransaction);
                context.setRecipientKey(recipientPublicKey);
                context.setBatchSize(batchSize);
                batchWorkflow.execute(context);
            });

        return new ResendBatchResponse(batchWorkflow.getPublishedMessageCount());
    }

    // TODO use some locking mechanism to make this more efficient
    @Override
    public synchronized void storeResendBatch(PushBatchRequest resendPushBatchRequest) {
        resendPushBatchRequest.getEncodedPayloads().stream()
            .map(StagingTransactionUtils::fromRawPayload)
            .forEach(stagingEntityDAO::save);
    }



}
