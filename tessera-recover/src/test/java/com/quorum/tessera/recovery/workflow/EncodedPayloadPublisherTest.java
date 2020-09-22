package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        batchWorkflowContext.setEncodedPayload(encodedPayload);
        PublicKey recipientKey = mock(PublicKey.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://junit.com");
        batchWorkflowContext.setRecipientKey(recipientKey);
        batchWorkflowContext.setRecipient(recipient);

        boolean result =
                IntStream.range(0, batchSize).allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

        assertThat(result).isTrue();

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

        List<EncodedPayload> sent = new ArrayList<>(batchSize);
        Collections.fill(sent, encodedPayload);

        verify(resendBatchPublisher, times(5)).publishBatch(sent, "http://junit.com");
    }

    @Test
    public void executeIfNumberOfPayloadsReachTotal() {

        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        batchWorkflowContext.setBatchSize(100);
        batchWorkflowContext.setExpectedTotal(9L);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        batchWorkflowContext.setEncodedPayload(encodedPayload);
        PublicKey recipientKey = mock(PublicKey.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://junit.com");
        batchWorkflowContext.setRecipientKey(recipientKey);
        batchWorkflowContext.setRecipient(recipient);

        boolean result =
                IntStream.range(0, batchSize).allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

        assertThat(result).isTrue();

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

        List<EncodedPayload> sent = new ArrayList<>(batchSize);
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

        encodedPayloadPublisher.execute(batchWorkflowContext);

        batchWorkflowContext.setExpectedTotal(3L);
        encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);
        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(0);

        encodedPayloadPublisher.execute(batchWorkflowContext);

        batchWorkflowContext.setExpectedTotal(2L);
        encodedPayloadPublisher.checkOutstandingPayloads(batchWorkflowContext);

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(2);

        List<EncodedPayload> sent = new ArrayList<>(2);
        Collections.fill(sent, encodedPayload);

        verify(resendBatchPublisher).publishBatch(sent, "http://junit.com");
    }

    @Test
    public void outstandingPayloadsPlusPublishedReachTotalExpected() {
        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        batchWorkflowContext.setBatchSize(2);
        batchWorkflowContext.setExpectedTotal(6L);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        batchWorkflowContext.setEncodedPayload(encodedPayload);
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
}
