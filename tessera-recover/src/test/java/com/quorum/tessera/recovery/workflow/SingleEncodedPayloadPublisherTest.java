package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class SingleEncodedPayloadPublisherTest {

  private PayloadPublisher payloadPublisher;

  private SingleEncodedPayloadPublisher workflowPublisher;

  @Before
  public void init() {
    this.payloadPublisher = mock(PayloadPublisher.class);

    this.workflowPublisher = new SingleEncodedPayloadPublisher(payloadPublisher);
  }

  @Test
  public void successfulPublishReturnsTrue() {
    final PublicKey sampleKey = PublicKey.from("testkey".getBytes());
    final EncodedPayload samplePayload1 =
        EncodedPayload.Builder.create().withSenderKey(sampleKey).build();
    final EncodedPayload samplePayload2 = EncodedPayload.Builder.create().build();

    final BatchWorkflowContext context = new BatchWorkflowContext();
    context.setRecipientKey(sampleKey);
    context.setPayloadsToPublish(Set.of(samplePayload1, samplePayload2));

    final boolean success = workflowPublisher.execute(context);

    assertThat(success).isTrue();

    verify(payloadPublisher).publishPayload(samplePayload1, sampleKey);
    verify(payloadPublisher).publishPayload(samplePayload2, sampleKey);
  }

  @Test
  public void unsuccessfulPublishReturnsFalse() {
    final PublicKey sampleKey = PublicKey.from("testkey".getBytes());
    final EncodedPayload samplePayload1 =
        EncodedPayload.Builder.create().withSenderKey(sampleKey).build();
    final EncodedPayload samplePayload2 = EncodedPayload.Builder.create().build();

    final BatchWorkflowContext context = new BatchWorkflowContext();
    context.setRecipientKey(sampleKey);
    context.setPayloadsToPublish(Set.of(samplePayload1, samplePayload2));

    doThrow(PublishPayloadException.class)
        .when(payloadPublisher)
        .publishPayload(samplePayload1, sampleKey);

    final boolean success = workflowPublisher.execute(context);

    assertThat(success).isFalse();

    verify(payloadPublisher).publishPayload(samplePayload1, sampleKey);
  }
}
