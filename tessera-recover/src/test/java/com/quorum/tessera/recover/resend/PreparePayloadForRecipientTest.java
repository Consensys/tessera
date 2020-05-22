package com.quorum.tessera.recover.resend;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PreparePayloadForRecipientTest {

    private PreparePayloadForRecipient preparePayloadForRecipient;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetup() {
        payloadEncoder = mock(PayloadEncoder.class);
        preparePayloadForRecipient = new PreparePayloadForRecipient(payloadEncoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder);
    }

    @Test
    public void executeWithSenderSameAsRecipientKey() {
        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        PublicKey recipientAndSenderKey = mock(PublicKey.class);
        when(encodedPayload.getSenderKey()).thenReturn(recipientAndSenderKey);
        encryptedTransactionEvent.setRecipientKey(recipientAndSenderKey);
        encryptedTransactionEvent.setEncodedPayload(encodedPayload);

        preparePayloadForRecipient.execute(encryptedTransactionEvent);

        assertThat(encryptedTransactionEvent.getEncodedPayload()).isSameAs(encodedPayload);

    }

    @Test
    public void execute() {
        BatchWorkflowContext encryptedTransactionEvent = new BatchWorkflowContext();

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        PublicKey recipientKey = mock(PublicKey.class);
        PublicKey senderKey = mock(PublicKey.class);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        encryptedTransactionEvent.setRecipientKey(recipientKey);
        encryptedTransactionEvent.setEncodedPayload(encodedPayload);

        EncodedPayload modifiedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.forRecipient(encodedPayload,recipientKey)).thenReturn(modifiedPayload);

        preparePayloadForRecipient.execute(encryptedTransactionEvent);

        assertThat(encryptedTransactionEvent.getEncodedPayload()).isSameAs(modifiedPayload);
        assertThat(encryptedTransactionEvent.getRecipientKey()).isSameAs(recipientKey);
        verify(payloadEncoder).forRecipient(encodedPayload,recipientKey);

    }
}
