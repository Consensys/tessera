package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

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
    public void targetKeyIsRecipientOfTransaction() {
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final EncodedPayload unformattedPayload =
                EncodedPayload.Builder.create()
                        .withRecipientKeys(List.of(targetResendKey))
                        .withRecipientBox("encrypteddata".getBytes())
                        .build();

        final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
        workflowEvent.setEncodedPayload(unformattedPayload);
        workflowEvent.setRecipientKey(targetResendKey);

        final EncodedPayload formattedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.forRecipient(unformattedPayload, targetResendKey)).thenReturn(formattedPayload);

        preparePayloadForRecipient.execute(workflowEvent);

        final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
        assertThat(payloadsToPublish).containsExactly(formattedPayload);

        verify(payloadEncoder).forRecipient(unformattedPayload, targetResendKey);
    }

    @Test
    public void targetKeyIsSenderOfTransactionWithRecipientsPresent() {
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

        final EncodedPayload unformattedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(targetResendKey)
                        .withRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBox("encrypteddata1".getBytes())
                        .withRecipientBox("encrypteddata2".getBytes())
                        .build();

        final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
        workflowEvent.setEncodedPayload(unformattedPayload);
        workflowEvent.setRecipientKey(targetResendKey);

        when(payloadEncoder.forRecipient(unformattedPayload, recipient1)).thenReturn(mock(EncodedPayload.class));
        when(payloadEncoder.forRecipient(unformattedPayload, recipient2)).thenReturn(mock(EncodedPayload.class));

        preparePayloadForRecipient.execute(workflowEvent);

        final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
        assertThat(payloadsToPublish).hasSize(2);

        verify(payloadEncoder).forRecipient(unformattedPayload, recipient1);
        verify(payloadEncoder).forRecipient(unformattedPayload, recipient2);
    }

    @Test
    public void targetKeyIsSenderOfTransactionWithNoRecipientsPresent() {
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());

        final EncodedPayload unformattedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(targetResendKey)
                        .withRecipientBox("encrypteddata1".getBytes())
                        .withRecipientBox("encrypteddata2".getBytes())
                        .build();

        final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
        workflowEvent.setEncodedPayload(unformattedPayload);
        workflowEvent.setRecipientKey(targetResendKey);

        preparePayloadForRecipient.execute(workflowEvent);

        final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
        assertThat(payloadsToPublish)
                .containsExactlyInAnyOrder(
                        EncodedPayload.Builder.create()
                                .withSenderKey(targetResendKey)
                                .withRecipientBox("encrypteddata1".getBytes())
                                .build(),
                        EncodedPayload.Builder.create()
                                .withSenderKey(targetResendKey)
                                .withRecipientBox("encrypteddata2".getBytes())
                                .build());
    }

    @Test
    public void psvTransactionOnlyUsesKeysWithBoxes() {
        final PublicKey targetResendKey = PublicKey.from("target".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

        final EncodedPayload unformattedPayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(targetResendKey)
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBox("encrypteddata1".getBytes())
                        .build();

        final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
        workflowEvent.setEncodedPayload(unformattedPayload);
        workflowEvent.setRecipientKey(targetResendKey);

        when(payloadEncoder.forRecipient(unformattedPayload, recipient1)).thenReturn(mock(EncodedPayload.class));
        when(payloadEncoder.forRecipient(unformattedPayload, recipient2)).thenReturn(mock(EncodedPayload.class));

        preparePayloadForRecipient.execute(workflowEvent);

        final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
        assertThat(payloadsToPublish).hasSize(1);

        verify(payloadEncoder).forRecipient(unformattedPayload, recipient1);
    }
}
