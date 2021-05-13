package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EncodedPayloadPublisher implements BatchWorkflowAction {

  private String targetUrl;

  private final List<Set<EncodedPayload>> payloads;

  private final ResendBatchPublisher resendBatchPublisher;

  private long messageCounter = 0L;

  public EncodedPayloadPublisher(ResendBatchPublisher resendBatchPublisher) {
    this.resendBatchPublisher = resendBatchPublisher;
    this.payloads = new ArrayList<>();
  }

  @Override
  public boolean execute(BatchWorkflowContext event) {
    final int batchSize = event.getBatchSize();
    final long total = event.getExpectedTotal();

    targetUrl = event.getRecipient().getUrl();
    payloads.add(event.getPayloadsToPublish());

    if (payloads.size() == batchSize || messageCounter + payloads.size() >= total) {
      publish(batchSize);
    }

    return true;
  }

  public long getPublishedCount() {
    return messageCounter;
  }

  public void checkOutstandingPayloads(BatchWorkflowContext event) {
    final long total = event.getExpectedTotal();
    final int noOfPayloads = payloads.size();

    if (noOfPayloads > 0 && (messageCounter + noOfPayloads >= total)) {
      publish(event.getBatchSize());
    }
  }

  private void publish(final int batchSize) {
    List<EncodedPayload> allPayloads =
        this.payloads.stream().flatMap(Set::stream).collect(Collectors.toList());

    // need to split the payloads into sublists with at most batchSize,
    // the publish each list individually
    while (allPayloads.size() > batchSize) {
      final List<EncodedPayload> sublistPayloads =
          new ArrayList<>(allPayloads.subList(0, batchSize));
      resendBatchPublisher.publishBatch(sublistPayloads, targetUrl);
      allPayloads = allPayloads.subList(batchSize, allPayloads.size());
    }
    // one final push for the last batch
    resendBatchPublisher.publishBatch(allPayloads, targetUrl);

    messageCounter += payloads.size();
    payloads.clear();
  }
}
