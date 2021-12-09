package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class LegacyWorkflowFactory {

  private final Enclave enclave;

  private final Discovery discovery;

  private final PayloadPublisher payloadPublisher;

  public LegacyWorkflowFactory(
      final Enclave enclave, final Discovery discovery, final PayloadPublisher payloadPublisher) {
    this.enclave = Objects.requireNonNull(enclave);
    this.discovery = Objects.requireNonNull(discovery);
    this.payloadPublisher = Objects.requireNonNull(payloadPublisher);
  }

  public BatchWorkflow create() {
    final ValidateEnclaveStatus validateEnclaveStatus = new ValidateEnclaveStatus(enclave);
    final StandardPrivateOnlyFilter standardPrivateOnlyFilter = new StandardPrivateOnlyFilter();
    final FilterPayload filterPayload = new FilterPayload(enclave);
    final PreparePayloadForRecipient preparePayloadForRecipient = new PreparePayloadForRecipient();
    final SearchRecipientKeyForPayload searchRecipientKeyForPayload =
        new SearchRecipientKeyForPayload(enclave);
    final FindRecipientFromPartyInfo findRecipientFromPartyInfo =
        new FindRecipientFromPartyInfo(discovery);
    final SenderIsNotRecipient senderIsNotRecipient = new SenderIsNotRecipient(enclave);
    final SingleEncodedPayloadPublisher encodedPayloadPublisher =
        new SingleEncodedPayloadPublisher(payloadPublisher);

    final List<BatchWorkflowAction> handlers =
        List.of(
            validateEnclaveStatus,
            standardPrivateOnlyFilter,
            filterPayload,
            preparePayloadForRecipient,
            searchRecipientKeyForPayload,
            findRecipientFromPartyInfo,
            senderIsNotRecipient,
            encodedPayloadPublisher);

    return new BatchWorkflow() {

      @Override
      public boolean execute(final BatchWorkflowContext context) {
        return handlers.stream()
            .filter(Predicate.not(h -> h.execute(context)))
            .findFirst()
            .isEmpty();
      }

      @Override
      public long getPublishedMessageCount() {
        return 0;
      }
    };
  }
}
