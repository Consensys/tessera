package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class SendRequestTest {

  @Test
  public void buildWithEverything() {
    byte[] payload = "Payload".getBytes();
    PublicKey sender = mock(PublicKey.class);
    PrivacyGroup.Id groupId = mock(PrivacyGroup.Id.class);
    List<PublicKey> recipients = List.of(mock(PublicKey.class));
    MessageHash affectedTransaction = mock(MessageHash.class);
    final byte[] execHash = "ExecHash".getBytes();
    SendRequest sendRequest =
        SendRequest.Builder.create()
            .withPayload(payload)
            .withSender(sender)
            .withRecipients(recipients)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash(execHash)
            .withAffectedContractTransactions(Set.of(affectedTransaction))
            .withPrivacyGroupId(groupId)
            .build();

    assertThat(sendRequest).isNotNull();
    assertThat(sendRequest.getSender()).isSameAs(sender);
    assertThat(sendRequest.getPayload()).containsExactly(payload);
    assertThat(sendRequest.getRecipients()).containsAll(recipients);
    assertThat(sendRequest.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(sendRequest.getExecHash()).containsExactly(execHash);
    assertThat(sendRequest.getAffectedContractTransactions()).containsExactly(affectedTransaction);

    assertThat(sendRequest.getPrivacyGroupId()).isPresent().get().isSameAs(groupId);
  }

  @Test(expected = NullPointerException.class)
  public void buildWithNothing() {
    SendRequest.Builder.create().build();
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutPayload() {
    PublicKey sender = mock(PublicKey.class);
    List<PublicKey> recipients = List.of(mock(PublicKey.class));

    SendRequest.Builder.create().withSender(sender).withRecipients(recipients).build();
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutSender() {
    byte[] payload = "Payload".getBytes();

    List<PublicKey> recipients = List.of(mock(PublicKey.class));

    SendRequest.Builder.create().withPayload(payload).withRecipients(recipients).build();
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutRecipients() {
    byte[] payload = "Payload".getBytes();
    PublicKey sender = mock(PublicKey.class);

    SendRequest.Builder.create().withPayload(payload).withSender(sender).build();
  }

  @Test(expected = RuntimeException.class)
  public void buildWithInvalidExecHash() {

    byte[] payload = "Payload".getBytes();
    PublicKey sender = mock(PublicKey.class);

    SendRequest.Builder.create()
        .withPayload(payload)
        .withSender(sender)
        .withRecipients(Collections.emptyList())
        .withAffectedContractTransactions(Collections.emptySet())
        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
        .withExecHash(new byte[0])
        .build();
  }

  @Test
  public void buildWithMandatoryRecipients() {
    byte[] payload = "Payload".getBytes();
    PublicKey sender = mock(PublicKey.class);
    PrivacyGroup.Id groupId = mock(PrivacyGroup.Id.class);
    List<PublicKey> recipients = List.of(mock(PublicKey.class));
    MessageHash affectedTransaction = mock(MessageHash.class);
    SendRequest sendRequest =
        SendRequest.Builder.create()
            .withPayload(payload)
            .withSender(sender)
            .withRecipients(recipients)
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withMandatoryRecipients(Set.of(PublicKey.from("key".getBytes())))
            .withAffectedContractTransactions(Set.of(affectedTransaction))
            .withPrivacyGroupId(groupId)
            .build();

    assertThat(sendRequest).isNotNull();
    assertThat(sendRequest.getSender()).isSameAs(sender);
    assertThat(sendRequest.getPayload()).containsExactly(payload);
    assertThat(sendRequest.getRecipients()).containsAll(recipients);
    assertThat(sendRequest.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(sendRequest.getAffectedContractTransactions()).containsExactly(affectedTransaction);
    assertThat(sendRequest.getMandatoryRecipients())
        .containsExactly(PublicKey.from("key".getBytes()));
    assertThat(sendRequest.getPrivacyGroupId()).isPresent().get().isSameAs(groupId);
  }

  @Test(expected = RuntimeException.class)
  public void buildWithMandatoryRecipientsInvalid() {
    byte[] payload = "Payload".getBytes();
    PublicKey sender = mock(PublicKey.class);
    List<PublicKey> recipients = List.of(mock(PublicKey.class));
    SendRequest.Builder.create()
        .withPayload(payload)
        .withSender(sender)
        .withRecipients(recipients)
        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
        .withMandatoryRecipients(Set.of(PublicKey.from("key".getBytes())))
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void buildWithNoMandatoryRecipientsData() {
    byte[] payload = "Payload".getBytes();
    PublicKey sender = mock(PublicKey.class);
    List<PublicKey> recipients = List.of(mock(PublicKey.class));
    SendRequest.Builder.create()
        .withPayload(payload)
        .withSender(sender)
        .withRecipients(recipients)
        .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
        .build();
  }
}
