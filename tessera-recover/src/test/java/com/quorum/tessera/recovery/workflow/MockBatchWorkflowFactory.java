package com.quorum.tessera.recovery.workflow;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class MockBatchWorkflowFactory implements BatchWorkflowFactory {

  private static final AtomicInteger EXECUTE_COUNTER = new AtomicInteger(0);

  static long transactionCount;

  static ResendBatchPublisher resendBatchPublisher;

  private static final ThreadLocal<SimpleBatchWorkflow> WORKFLOW =
      ThreadLocal.withInitial(SimpleBatchWorkflow::new);

  @Override
  public BatchWorkflow create() {
    return WORKFLOW.get();
  }

  static SimpleBatchWorkflow getWorkflow() {
    return WORKFLOW.get();
  }

  static void reset() {
    WORKFLOW.remove();
    EXECUTE_COUNTER.set(0);
  }

  public static class SimpleBatchWorkflow implements BatchWorkflow {

    private EncodedPayload singlePayloadToPublish;

    BatchWorkflowAction findRecipientFromPartyInfo =
        context -> {
          context.setRecipient(mock(Recipient.class));
          return true;
        };
    BatchWorkflowAction setPayloadsToPublish =
        context -> {
          context.setPayloadsToPublish(Set.of(singlePayloadToPublish));
          return true;
        };
    EncodedPayloadPublisher encodedPayloadPublisher =
        new EncodedPayloadPublisher(resendBatchPublisher);

    List<BatchWorkflowAction> handlers =
        List.of(findRecipientFromPartyInfo, setPayloadsToPublish, encodedPayloadPublisher);

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

      EXECUTE_COUNTER.incrementAndGet();

      return outcome;
    }

    @Override
    public long getPublishedMessageCount() {
      return encodedPayloadPublisher.getPublishedCount();
    }

    public void setSinglePayloadToPublish(EncodedPayload singlePayloadToPublish) {
      this.singlePayloadToPublish = singlePayloadToPublish;
    }
  }

  static int getExecuteInvocationCounter() {
    return EXECUTE_COUNTER.get();
  }
}
