package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        final EncodedPayload samplePayload = EncodedPayload.Builder.create().build();

        final BatchWorkflowContext context = new BatchWorkflowContext();
        context.setRecipientKey(sampleKey);
        context.setEncodedPayload(samplePayload);

        final boolean success = workflowPublisher.execute(context);

        assertThat(success).isTrue();

        verify(payloadPublisher).publishPayload(samplePayload, sampleKey);
    }

    @Test
    public void unsuccessfulPublishReturnsFalse() {
        final PublicKey sampleKey = PublicKey.from("testkey".getBytes());
        final EncodedPayload samplePayload = EncodedPayload.Builder.create().build();

        final BatchWorkflowContext context = new BatchWorkflowContext();
        context.setRecipientKey(sampleKey);
        context.setEncodedPayload(samplePayload);

        doThrow(PublishPayloadException.class).when(payloadPublisher).publishPayload(samplePayload, sampleKey);

        final boolean success = workflowPublisher.execute(context);

        assertThat(success).isFalse();

        verify(payloadPublisher).publishPayload(samplePayload, sampleKey);
    }

}
