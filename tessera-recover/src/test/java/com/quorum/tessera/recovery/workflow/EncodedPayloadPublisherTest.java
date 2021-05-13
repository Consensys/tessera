package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EncodedPayloadPublisherTest {

  private EncodedPayloadPublisher encodedPayloadPublisher;

  private ResendBatchPublisher resendBatchPublisher;

  private final int batchSize = 9;

  @Before
  public void onSetUp() {
    resendBatchPublisher = mock(ResendBatchPublisher.class);

    encodedPayloadPublisher = new EncodedPayloadPublisher(resendBatchPublisher);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(resendBatchPublisher);
  }

  @Test
  public void executeIfNumberOfPayloadsReachBatchSize() {
    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    batchWorkflowContext.setBatchSize(2);
    batchWorkflowContext.setExpectedTotal(9L);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    batchWorkflowContext.setPayloadsToPublish(Set.of(encodedPayload));
    PublicKey recipientKey = mock(PublicKey.class);
    Recipient recipient = mock(Recipient.class);
    when(recipient.getUrl()).thenReturn("http://junit.com");
    batchWorkflowContext.setRecipientKey(recipientKey);
    batchWorkflowContext.setRecipient(recipient);

    boolean result =
        IntStream.range(0, batchSize)
            .allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

    assertThat(result).isTrue();

    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

    List<EncodedPayload> sent2 = List.of(encodedPayload, encodedPayload);
    List<EncodedPayload> sent1 = List.of(encodedPayload);

    verify(resendBatchPublisher, times(4)).publishBatch(sent2, "http://junit.com");
    verify(resendBatchPublisher).publishBatch(sent1, "http://junit.com");
  }

  @Test
  public void executeIfNumberOfPayloadsReachTotal() {
    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    batchWorkflowContext.setBatchSize(100);
    batchWorkflowContext.setExpectedTotal(9L);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    batchWorkflowContext.setPayloadsToPublish(Set.of(encodedPayload));
    PublicKey recipientKey = mock(PublicKey.class);
    Recipient recipient = mock(Recipient.class);
    when(recipient.getUrl()).thenReturn("http://junit.com");
    batchWorkflowContext.setRecipientKey(recipientKey);
    batchWorkflowContext.setRecipient(recipient);

    boolean result =
        IntStream.range(0, batchSize)
            .allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

    assertThat(result).isTrue();

    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

    List<EncodedPayload> sent = Arrays.asList(new EncodedPayload[9]);
    Collections.fill(sent, encodedPayload);

    verify(resendBatchPublisher).publishBatch(sent, "http://junit.com");
  }

  @Test
  public void noOutstandingPayload() {
    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    batchWorkflowContext.setBatchSize(100);
    batchWorkflowContext.setExpectedTotal(4L);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    batchWorkflowContext.setEncodedPayload(encodedPayload);
    PublicKey recipientKey = mock(PublicKey.class);
    Recipient recipient = mock(Recipient.class);
    when(recipient.getUrl()).thenReturn("http://junit.com");
    batchWorkflowContext.setRecipientKey(recipientKey);
    batchWorkflowContext.setRecipient(recipient);

    encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(0);
  }

  @Test
  public void outstandingPayloadsReachTotalExpected() {
    final PublicKey recipientKey = PublicKey.from("test-key".getBytes());
    EncodedPayload encodedPayload = mock(EncodedPayload.class);

    final BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    batchWorkflowContext.setBatchSize(100);
    batchWorkflowContext.setExpectedTotal(4L);
    batchWorkflowContext.setPayloadsToPublish(Set.of(encodedPayload));
    batchWorkflowContext.setRecipientKey(recipientKey);
    batchWorkflowContext.setRecipient(Recipient.of(recipientKey, "http://junit.com"));

    encodedPayloadPublisher.execute(batchWorkflowContext);

    batchWorkflowContext.setExpectedTotal(3L);
    encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(0);

    encodedPayloadPublisher.execute(batchWorkflowContext);

    batchWorkflowContext.setExpectedTotal(2L);
    encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);

    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(2);

    List<EncodedPayload> sent = List.of(encodedPayload, encodedPayload);

    verify(resendBatchPublisher).publishBatch(sent, "http://junit.com");
  }

  @Test
  public void outstandingPayloadsPlusPublishedReachTotalExpected() {
    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    batchWorkflowContext.setBatchSize(2);
    batchWorkflowContext.setExpectedTotal(6L);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    batchWorkflowContext.setPayloadsToPublish(Set.of(encodedPayload));
    PublicKey recipientKey = mock(PublicKey.class);
    Recipient recipient = mock(Recipient.class);
    when(recipient.getUrl()).thenReturn("http://junit.com");
    batchWorkflowContext.setRecipientKey(recipientKey);
    batchWorkflowContext.setRecipient(recipient);

    encodedPayloadPublisher.execute(batchWorkflowContext);

    batchWorkflowContext.setExpectedTotal(5L);
    encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(0);

    encodedPayloadPublisher.execute(batchWorkflowContext);

    batchWorkflowContext.setExpectedTotal(4L);
    encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(2);

    // Not publish yet as not yet reach batch size
    encodedPayloadPublisher.execute(batchWorkflowContext);
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(2);

    batchWorkflowContext.setExpectedTotal(3L);
    encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);
    // Publish here because message count + outstanding = expected total
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(3);

    verify(resendBatchPublisher, times(2)).publishBatch(anyList(), eq("http://junit.com"));
  }

  @Test
  public void flattenedPayloadsOverBatchSizeSplitsToSublists() {
    final int numberOfMessagesToSend = 10;
    PublicKey recipientKey = mock(PublicKey.class);
    Recipient recipient = Recipient.of(recipientKey, "http://junit.com");
    EncodedPayload encodedPayloadFirst = mock(EncodedPayload.class);
    EncodedPayload encodedPayloadSecond = mock(EncodedPayload.class);

    BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
    batchWorkflowContext.setBatchSize(3);
    batchWorkflowContext.setExpectedTotal(10L);
    batchWorkflowContext.setPayloadsToPublish(
        new LinkedHashSet<>(List.of(encodedPayloadFirst, encodedPayloadSecond)));
    batchWorkflowContext.setRecipientKey(recipientKey);
    batchWorkflowContext.setRecipient(recipient);

    boolean result =
        IntStream.range(0, numberOfMessagesToSend)
            .allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

    assertThat(result).isTrue();
    assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(10);

    List<EncodedPayload> batchOne =
        List.of(encodedPayloadFirst, encodedPayloadSecond, encodedPayloadFirst);
    List<EncodedPayload> batchTwo =
        List.of(encodedPayloadSecond, encodedPayloadFirst, encodedPayloadSecond);
    List<EncodedPayload> leftovers = List.of(encodedPayloadFirst, encodedPayloadSecond);

    verify(resendBatchPublisher, times(3)).publishBatch(batchOne, "http://junit.com");
    verify(resendBatchPublisher, times(3)).publishBatch(batchTwo, "http://junit.com");
    verify(resendBatchPublisher).publishBatch(leftovers, "http://junit.com");
  }
}
