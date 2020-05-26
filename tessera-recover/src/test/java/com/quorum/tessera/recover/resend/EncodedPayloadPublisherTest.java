package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.model.Recipient;
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
    public void executeSingleBatch() {

        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        batchWorkflowContext.setBatchSize(batchSize);
        batchWorkflowContext.setExpectedTotal(999L);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        batchWorkflowContext.setEncodedPayload(encodedPayload);
        PublicKey recipientKey = mock(PublicKey.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://junit.com");

        batchWorkflowContext.setRecipientKey(recipientKey);
        batchWorkflowContext.setRecipient(recipient);

        boolean result = IntStream.range(0,batchSize).allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

        assertThat(result).isTrue();

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

        List<EncodedPayload> sent = new ArrayList<>(batchSize);
        Collections.fill(sent,encodedPayload);

        verify(resendBatchPublisher).publishBatch(sent,"http://junit.com");

    }

    @Test
    public void executePartialBatch() {

        BatchWorkflowContext batchWorkflowContext = new BatchWorkflowContext();
        batchWorkflowContext.setBatchSize(batchSize);
        batchWorkflowContext.setExpectedTotal(999L);
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        batchWorkflowContext.setEncodedPayload(encodedPayload);
        PublicKey recipientKey = mock(PublicKey.class);
        Recipient recipient = mock(Recipient.class);
        when(recipient.getUrl()).thenReturn("http://junit.com");

        batchWorkflowContext.setRecipientKey(recipientKey);
        batchWorkflowContext.setRecipient(recipient);


        boolean result = IntStream.range(0,batchSize)
            .allMatch(i -> encodedPayloadPublisher.execute(batchWorkflowContext));

        assertThat(result).isTrue();

        assertThat(encodedPayloadPublisher.getPublishedCount()).isEqualTo(9);

        List<EncodedPayload> sent = new ArrayList<>(batchSize);
        Collections.fill(sent,encodedPayload);

        verify(resendBatchPublisher).publishBatch(sent,"http://junit.com");

    }
}
