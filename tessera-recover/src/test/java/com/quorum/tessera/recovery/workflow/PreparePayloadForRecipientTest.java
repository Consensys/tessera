package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.UNSUPPORTED;
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final EncodedPayload unformattedPayload = mock(EncodedPayload.class);
    when(unformattedPayload.getEncodedPayloadCodec()).thenReturn(encodedPayloadCodec);

    RecipientBox recipientBox = mock(RecipientBox.class);
    when(recipientBox.getData()).thenReturn("encrypteddata".getBytes());
    when(unformattedPayload.getRecipientKeys()).thenReturn(List.of(targetResendKey));
    when(unformattedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(unformattedPayload);
    workflowEvent.setRecipientKey(targetResendKey);

    final EncodedPayload formattedPayload = mock(EncodedPayload.class);
    when(payloadEncoder.forRecipient(unformattedPayload, targetResendKey))
        .thenReturn(formattedPayload);

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

    EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.UNSUPPORTED;

    final EncodedPayload unformattedPayload = mock(EncodedPayload.class);
    when(unformattedPayload.getEncodedPayloadCodec()).thenReturn(encodedPayloadCodec);

    RecipientBox recipientBox = mock(RecipientBox.class);
    when(recipientBox.getData()).thenReturn("encrypteddata1".getBytes());
    RecipientBox recipientBox2 = mock(RecipientBox.class);
    when(recipientBox2.getData()).thenReturn("encrypteddata2".getBytes());

    when(unformattedPayload.getSenderKey()).thenReturn(targetResendKey);
    when(unformattedPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    when(unformattedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox, recipientBox2));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(unformattedPayload);
    workflowEvent.setRecipientKey(targetResendKey);

    when(payloadEncoder.forRecipient(unformattedPayload, recipient1))
        .thenReturn(mock(EncodedPayload.class));
    when(payloadEncoder.forRecipient(unformattedPayload, recipient2))
        .thenReturn(mock(EncodedPayload.class));

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(2);

    verify(payloadEncoder).forRecipient(unformattedPayload, recipient1);
    verify(payloadEncoder).forRecipient(unformattedPayload, recipient2);
  }

  @Test
  public void targetKeyIsSenderOfTransactionWithNoRecipientsPresent() {
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.UNSUPPORTED;

    final EncodedPayload unformattedPayload = mock(EncodedPayload.class);
    when(unformattedPayload.getSenderKey()).thenReturn(targetResendKey);
    when(unformattedPayload.getEncodedPayloadCodec()).thenReturn(encodedPayloadCodec);

    List<RecipientBox> recipientBoxes =
        Stream.of("encrypteddata1", "encrypteddata2")
            .map(String::getBytes)
            .map(
                data -> {
                  RecipientBox recipientBox = mock(RecipientBox.class);
                  when(recipientBox.getData()).thenReturn(data);
                  return recipientBox;
                })
            .collect(Collectors.toUnmodifiableList());
    ;

    when(unformattedPayload.getRecipientBoxes()).thenReturn(recipientBoxes);

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(unformattedPayload);
    workflowEvent.setRecipientKey(targetResendKey);

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(2);

    for (EncodedPayload encodedPayload : payloadsToPublish) {
      assertThat(encodedPayload.getSenderKey()).isEqualTo(targetResendKey);
      assertThat(encodedPayload.getRecipientBoxes()).hasSize(1);
      RecipientBox recipientBox = encodedPayload.getRecipientBoxes().iterator().next();
      assertThat(recipientBox.getData()).startsWith("encrypteddata".getBytes());
    }
  }

  @Test
  public void psvTransactionOnlyUsesKeysWithBoxes() {
    final PublicKey targetResendKey = PublicKey.from("target".getBytes());
    final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
    final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

    final EncodedPayload unformattedPayload = mock(EncodedPayload.class);

    when(unformattedPayload.getSenderKey()).thenReturn(targetResendKey);
    when(unformattedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(unformattedPayload.getExecHash()).thenReturn("execHash".getBytes());
    when(unformattedPayload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
    RecipientBox recipientBox = mock(RecipientBox.class);
    when(recipientBox.getData()).thenReturn("encrypteddata1".getBytes());
    when(unformattedPayload.getRecipientBoxes()).thenReturn(List.of(recipientBox));

    final BatchWorkflowContext workflowEvent = new BatchWorkflowContext();
    workflowEvent.setEncodedPayload(unformattedPayload);
    workflowEvent.setRecipientKey(targetResendKey);

    when(payloadEncoder.forRecipient(unformattedPayload, recipient1))
        .thenReturn(mock(EncodedPayload.class));
    when(payloadEncoder.forRecipient(unformattedPayload, recipient2))
        .thenReturn(mock(EncodedPayload.class));

    preparePayloadForRecipient.execute(workflowEvent);

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(1);

    verify(payloadEncoder).forRecipient(unformattedPayload, recipient1);
  }
}
