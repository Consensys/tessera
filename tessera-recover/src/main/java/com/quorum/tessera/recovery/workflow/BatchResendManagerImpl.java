package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransactionUtils;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.PushBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.resend.ResendBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
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

  private final Discovery discovery;

  private final ResendBatchPublisher resendBatchPublisher;

  private final int maxResults;

  public BatchResendManagerImpl(
      Enclave enclave,
      StagingEntityDAO stagingEntityDAO,
      EncryptedTransactionDAO encryptedTransactionDAO,
      Discovery discovery,
      ResendBatchPublisher resendBatchPublisher,
      int maxResults) {
    this(
        PayloadEncoder.create(),
        Base64Codec.create(),
        enclave,
        stagingEntityDAO,
        encryptedTransactionDAO,
        discovery,
        resendBatchPublisher,
        maxResults);
  }

  public BatchResendManagerImpl(
      PayloadEncoder payloadEncoder,
      Base64Codec base64Decoder,
      Enclave enclave,
      StagingEntityDAO stagingEntityDAO,
      EncryptedTransactionDAO encryptedTransactionDAO,
      Discovery discovery,
      ResendBatchPublisher resendBatchPublisher,
      int maxResults) {
    this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
    this.base64Decoder = Objects.requireNonNull(base64Decoder);
    this.enclave = Objects.requireNonNull(enclave);
    this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
    this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
    this.discovery = Objects.requireNonNull(discovery);
    this.resendBatchPublisher = Objects.requireNonNull(resendBatchPublisher);
    this.maxResults = maxResults;
  }

  static int calculateBatchCount(long maxResults, long total) {
    return (int) Math.ceil((double) total / maxResults);
  }

  @Override
  public ResendBatchResponse resendBatch(ResendBatchRequest request) {

    final int batchSize = validateRequestBatchSize(request.getBatchSize());
    final byte[] publicKeyData = base64Decoder.decode(request.getPublicKey());
    final PublicKey recipientPublicKey = PublicKey.from(publicKeyData);

    final long transactionCount = encryptedTransactionDAO.transactionCount();
    final long batchCount = calculateBatchCount(maxResults, transactionCount);

    final BatchWorkflow batchWorkflow =
        BatchWorkflowFactory.newFactory(
                enclave, payloadEncoder, discovery, resendBatchPublisher, transactionCount)
            .create();

    IntStream.range(0, (int) batchCount)
        .map(i -> i * maxResults)
        .mapToObj(offset -> encryptedTransactionDAO.retrieveTransactions(offset, maxResults))
        .flatMap(List::stream)
        .forEach(
            encryptedTransaction -> {
              final BatchWorkflowContext context = new BatchWorkflowContext();
              context.setEncryptedTransaction(encryptedTransaction);
              context.setRecipientKey(recipientPublicKey);
              context.setBatchSize(batchSize);
              batchWorkflow.execute(context);
            });

    return ResendBatchResponse.from(batchWorkflow.getPublishedMessageCount());
  }

  @Override
  public synchronized void storeResendBatch(PushBatchRequest resendPushBatchRequest) {
    resendPushBatchRequest.getEncodedPayloads().stream()
        .map(StagingTransactionUtils::fromRawPayload)
        .forEach(stagingEntityDAO::save);
  }

  private int validateRequestBatchSize(int s) {
    if (Math.max(1, s) == Math.min(s, maxResults)) {
      return s;
    }
    return maxResults;
  }
}
