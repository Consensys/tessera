package com.quorum.tessera.recovery.workflow.internal;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransactionUtils;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.recovery.resend.PushBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.BatchWorkflow;
import com.quorum.tessera.recovery.workflow.BatchWorkflowContext;
import com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class BatchResendManagerImpl implements BatchResendManager {

  private final StagingEntityDAO stagingEntityDAO;

  private final EncryptedTransactionDAO encryptedTransactionDAO;

  private final int maxResults;

  private final BatchWorkflowFactory batchWorkflowFactory;

  public BatchResendManagerImpl(
      StagingEntityDAO stagingEntityDAO,
      EncryptedTransactionDAO encryptedTransactionDAO,
      int maxResults,
      BatchWorkflowFactory batchWorkflowFactory) {

    this.stagingEntityDAO = Objects.requireNonNull(stagingEntityDAO);
    this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
    this.maxResults = maxResults;

    this.batchWorkflowFactory = batchWorkflowFactory;
  }

  static int calculateBatchCount(long maxResults, long total) {
    return (int) Math.ceil((double) total / maxResults);
  }

  @Override
  public ResendBatchResponse resendBatch(ResendBatchRequest request) {

    final int batchSize = validateRequestBatchSize(request.getBatchSize());
    final byte[] publicKeyData = Base64.getDecoder().decode(request.getPublicKey());
    final PublicKey recipientPublicKey = PublicKey.from(publicKeyData);

    final long transactionCount = encryptedTransactionDAO.transactionCount();
    final long batchCount = calculateBatchCount(maxResults, transactionCount);

    final BatchWorkflow batchWorkflow = batchWorkflowFactory.create(transactionCount);

    IntStream.range(0, (int) batchCount)
        .map(i -> i * maxResults)
        .mapToObj(offset -> encryptedTransactionDAO.retrieveTransactions(offset, maxResults))
        .flatMap(List::stream)
        .forEach(
            encryptedTransaction -> {
              final BatchWorkflowContext context = new BatchWorkflowContext();
              context.setEncryptedTransaction(encryptedTransaction);
              context.setEncodedPayload(encryptedTransaction.getPayload());
              context.setRecipientKey(recipientPublicKey);
              context.setBatchSize(batchSize);
              batchWorkflow.execute(context);
            });

    return ResendBatchResponse.from(batchWorkflow.getPublishedMessageCount());
  }

  @Override
  public synchronized void storeResendBatch(PushBatchRequest request) {
    request.getEncodedPayloads().stream()
        .map(p -> StagingTransactionUtils.fromRawPayload(p, request.getEncodedPayloadCodec()))
        .forEach(stagingEntityDAO::save);
  }

  private int validateRequestBatchSize(int s) {
    if (Math.max(1, s) == Math.min(s, maxResults)) {
      return s;
    }
    return maxResults;
  }
}
