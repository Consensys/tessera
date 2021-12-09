package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getRecipientKeys()).thenReturn(List.of(targetResendKey));
    when(payload.getRecipientBoxes()).thenReturn(List.of(mock(RecipientBox.class)));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(payload);
    workflowEvent.setRecipientKey(targetResendKey);

    final EncodedPayload formattedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.forRecipient(payload, targetResendKey)).thenReturn(formattedPayload);

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).containsExactly(formattedPayload);

    verify(payloadEncoder).forRecipient(payload, targetResendKey);
  }

  @Test
  public void targetKeyIsSenderOfTransactionWithRecipientsPresent() {
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(targetResendKey);
    when(payload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    when(payload.getRecipientBoxes())
        .thenReturn(List.of(mock(RecipientBox.class), mock(RecipientBox.class)));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(payload);
    workflowEvent.setRecipientKey(targetResendKey);

    when(payloadEncoder.forRecipient(payload, recipient1)).thenReturn(mock(EncodedPayload.class));
    when(payloadEncoder.forRecipient(payload, recipient2)).thenReturn(mock(EncodedPayload.class));

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(2);

    verify(payloadEncoder).forRecipient(payload, recipient1);
    verify(payloadEncoder).forRecipient(payload, recipient2);
  }

  @Test
  public void targetKeyIsSenderOfTransactionWithNoRecipientsPresent() {
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(targetResendKey);
    when(payload.getRecipientBoxes())
        .thenReturn(
            List.of(RecipientBox.from("box1".getBytes()), RecipientBox.from("box2".getBytes())));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(payload);
    workflowEvent.setRecipientKey(targetResendKey);

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish.size()).isEqualTo(2);
    assertThat(
            payloadsToPublish.stream()
                .map(EncodedPayload::getSenderKey)
                .filter(targetResendKey::equals)
                .count())
        .isEqualTo(2);
    assertThat(
            payloadsToPublish.stream()
                .map(EncodedPayload::getRecipientBoxes)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()))
        .containsExactlyInAnyOrder(
            RecipientBox.from("box1".getBytes()), RecipientBox.from("box2".getBytes()));
  }

  @Test
  public void psvTransactionOnlyUsesKeysWithBoxes() {
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getSenderKey()).thenReturn(targetResendKey);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(payload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    when(payload.getRecipientBoxes()).thenReturn(List.of(RecipientBox.from("box1".getBytes())));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(payload);
    workflowEvent.setRecipientKey(targetResendKey);

    when(payloadEncoder.forRecipient(payload, recipient1)).thenReturn(mock(EncodedPayload.class));
    when(payloadEncoder.forRecipient(payload, recipient2)).thenReturn(mock(EncodedPayload.class));

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(1);

    verify(payloadEncoder).forRecipient(payload, recipient1);
  }
}
