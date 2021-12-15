package com.quorum.tessera.recovery.workflow.internal;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.workflow.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

class BatchWorkflowFactoryImpl implements BatchWorkflowFactory {

  private final Enclave enclave;

  private final Discovery discovery;

  private final ResendBatchPublisher resendBatchPublisher;

  BatchWorkflowFactoryImpl(
      Enclave enclave, Discovery discovery, ResendBatchPublisher resendBatchPublisher) {
    this.enclave = Objects.requireNonNull(enclave);
    this.discovery = Objects.requireNonNull(discovery);
    this.resendBatchPublisher = Objects.requireNonNull(resendBatchPublisher);
  }

  @Override
  public BatchWorkflow create(long transactionCount) {

    ValidateEnclaveStatus validateEnclaveStatus = new ValidateEnclaveStatus(enclave);
    PreparePayloadForRecipient preparePayloadForRecipient = new PreparePayloadForRecipient();
    FindRecipientFromPartyInfo findRecipientFromPartyInfo =
        new FindRecipientFromPartyInfo(discovery);
    FilterPayload filterPayload = new FilterPayload(enclave);
    SearchRecipientKeyForPayload searchRecipientKeyForPayload =
        new SearchRecipientKeyForPayload(enclave);
    SenderIsNotRecipient senderIsNotRecipient = new SenderIsNotRecipient(enclave);
    EncodedPayloadPublisher encodedPayloadPublisher =
        new EncodedPayloadPublisher(resendBatchPublisher);

    List<BatchWorkflowAction> handlers =
        List.of(
            validateEnclaveStatus,
            filterPayload,
            preparePayloadForRecipient,
            searchRecipientKeyForPayload,
            findRecipientFromPartyInfo,
            senderIsNotRecipient,
            encodedPayloadPublisher);

    return new BatchWorkflow() {

      private final AtomicLong filteredMessageCount = new AtomicLong(transactionCount);

      @Override
      public boolean execute(BatchWorkflowContext context) {

        context.setExpectedTotal(filteredMessageCount.get());

        boolean outcome =
            handlers.stream().filter(Predicate.not(h -> h.execute(context))).findFirst().isEmpty();

        if (!outcome) {
          context.setExpectedTotal(filteredMessageCount.decrementAndGet());
          encodedPayloadPublisher.checkOutstandingPayloads(context);
        }
        return outcome;
      }

      @Override
      public long getPublishedMessageCount() {
        return encodedPayloadPublisher.getPublishedCount();
      }
    };
  }
}
