package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class PreparePayloadForRecipientTest {

  private PreparePayloadForRecipient preparePayloadForRecipient;

  @Before
  public void onSetup() {
    preparePayloadForRecipient = new PreparePayloadForRecipient();
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
    try (var mockStatic = mockStatic(EncodedPayload.Builder.class)) {
      EncodedPayload.Builder builder = mock(EncodedPayload.Builder.class);
      mockStatic
          .when(() -> EncodedPayload.Builder.forRecipient(payload, targetResendKey))
          .thenReturn(builder);
      when(builder.build()).thenReturn(formattedPayload);

      preparePayloadForRecipient.execute(workflowEvent);

      mockStatic.verify(() -> EncodedPayload.Builder.forRecipient(payload, targetResendKey));
    }

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).containsExactly(formattedPayload);
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

    try (var mockStatic = mockStatic(EncodedPayload.Builder.class)) {
      EncodedPayload.Builder builder1 = mock(EncodedPayload.Builder.class);
      when(builder1.build()).thenReturn(mock(EncodedPayload.class));
      EncodedPayload.Builder builder2 = mock(EncodedPayload.Builder.class);
      when(builder2.build()).thenReturn(mock(EncodedPayload.class));
      mockStatic
          .when(() -> EncodedPayload.Builder.forRecipient(payload, recipient1))
          .thenReturn(builder1);
      mockStatic
          .when(() -> EncodedPayload.Builder.forRecipient(payload, recipient2))
          .thenReturn(builder2);

      preparePayloadForRecipient.execute(workflowEvent);

      mockStatic.verify(() -> EncodedPayload.Builder.forRecipient(payload, recipient1));
      mockStatic.verify(() -> EncodedPayload.Builder.forRecipient(payload, recipient2));

      verify(builder1).build();
      verify(builder2).build();
    }

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(2);
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

    try (var mockStatic = mockStatic(EncodedPayload.Builder.class)) {
      EncodedPayload.Builder builder1 = mock(EncodedPayload.Builder.class);
      when(builder1.build()).thenReturn(mock(EncodedPayload.class));
      EncodedPayload.Builder builder2 = mock(EncodedPayload.Builder.class);
      when(builder2.build()).thenReturn(mock(EncodedPayload.class));
      mockStatic
          .when(() -> EncodedPayload.Builder.forRecipient(payload, recipient1))
          .thenReturn(builder1);
      mockStatic
          .when(() -> EncodedPayload.Builder.forRecipient(payload, recipient2))
          .thenReturn(builder2);

      preparePayloadForRecipient.execute(workflowEvent);

      mockStatic.verify(() -> EncodedPayload.Builder.forRecipient(payload, recipient1));

      verify(builder1).build();

      verifyNoMoreInteractions(builder1, builder2);
    }

    final Set<EncodedPayload> payloadsToPublish = workflowEvent.getPayloadsToPublish();
    assertThat(payloadsToPublish).hasSize(1);
  }
}
