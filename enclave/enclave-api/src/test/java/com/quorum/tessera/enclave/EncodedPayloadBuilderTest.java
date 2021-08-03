package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class EncodedPayloadBuilderTest {

  private final String sampleTxHash =
      "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";

  final PublicKey senderKey = PublicKey.from("SENDER_KEY".getBytes());

  final PublicKey recipientKey = PublicKey.from("RECIPIENT_KEY".getBytes());

  final byte[] cipherText = "cipherText".getBytes();

  final byte[] cipherTextNonce = "cipherTextNonce".getBytes();

  final byte[] recipientNonce = "recipientNonce".getBytes();

  final byte[] recipientBox = "recipientBox".getBytes();

  final Map<TxHash, byte[]> affectedContractTransactionsRaw =
      Map.of(new TxHash(sampleTxHash), "transaction".getBytes());

  final byte[] execHash = "execHash".getBytes();

  @Test
  public void build() {
    final EncodedPayload sample =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox)
            .withRecipientNonce(recipientNonce)
            .withPrivacyFlag(3)
            .withAffectedContractTransactions(affectedContractTransactionsRaw)
            .withExecHash(execHash)
            .withRecipientKey(recipientKey)
            .build();

    assertThat(sample.getSenderKey()).isEqualTo(senderKey);
    assertThat(sample.getCipherText()).isEqualTo("cipherText".getBytes());
    assertThat(sample.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
    assertThat(sample.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
    assertThat(sample.getRecipientBoxes())
        .hasSize(1)
        .containsExactlyInAnyOrder(RecipientBox.from(recipientBox));
    assertThat(sample.getRecipientKeys()).hasSize(1).containsExactlyInAnyOrder(recipientKey);
    assertThat(sample.getAffectedContractTransactions()).hasSize(1);
    assertThat(sample.getAffectedContractTransactions().keySet())
        .containsExactly(new TxHash(sampleTxHash));
    assertThat(sample.getExecHash()).isEqualTo(execHash);
    assertThat(sample.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);

    byte[] otherRecipientBox = "OTHETRBIX".getBytes();
    EncodedPayload fromSample =
        EncodedPayload.Builder.from(sample).withRecipientBox(otherRecipientBox).build();

    assertThat(fromSample.getRecipientBoxes())
        .containsExactly(RecipientBox.from(recipientBox), RecipientBox.from(otherRecipientBox));
  }

  @Test
  public void withNewKeysReplacedOld() {
    final EncodedPayload sample =
        EncodedPayload.Builder.create().withRecipientKey(recipientKey).build();

    assertThat(sample.getRecipientKeys()).containsExactly(recipientKey);

    final PublicKey replacementKey = PublicKey.from("replacement".getBytes());
    final EncodedPayload updatedPayload =
        EncodedPayload.Builder.from(sample).withNewRecipientKeys(List.of(replacementKey)).build();

    assertThat(updatedPayload.getRecipientKeys()).containsExactly(replacementKey);
  }

  @Test
  public void from() {
    final EncodedPayload sample =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(List.of(recipientBox))
            .withRecipientNonce(recipientNonce)
            .withRecipientKeys(List.of(recipientKey))
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withExecHash(execHash)
            .build();

    EncodedPayload result = EncodedPayload.Builder.from(sample).build();

    assertThat(result).isNotSameAs(sample).isEqualTo(sample);

    EqualsVerifier.forClass(EncodedPayload.class)
        .withIgnoredFields("affectedContractTransactions")
        .usingGetClass()
        .verify();
  }

  @Test
  public void withPrivacyGroupId() {
    final EncodedPayload sample =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox)
            .withRecipientNonce(recipientNonce)
            .withPrivacyFlag(3)
            .withAffectedContractTransactions(affectedContractTransactionsRaw)
            .withExecHash(execHash)
            .withRecipientKey(recipientKey)
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("PRIVACYGROUPID".getBytes()))
            .build();

    final EncodedPayload result = EncodedPayload.Builder.from(sample).build();
    assertThat(result).isNotSameAs(sample).isEqualTo(sample);

    EqualsVerifier.forClass(EncodedPayload.class)
        .withIgnoredFields("affectedContractTransactions")
        .usingGetClass()
        .verify();

    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyGroupId().get())
        .isEqualTo(PrivacyGroup.Id.fromBytes("PRIVACYGROUPID".getBytes()));
  }

  @Test(expected = RuntimeException.class)
  public void nonPSVButExecHashPresent() {
    EncodedPayload.Builder.create()
        .withSenderKey(senderKey)
        .withCipherText(cipherText)
        .withCipherTextNonce(cipherTextNonce)
        .withRecipientBox(recipientBox)
        .withRecipientNonce(recipientNonce)
        .withPrivacyFlag(1)
        .withAffectedContractTransactions(affectedContractTransactionsRaw)
        .withExecHash(execHash)
        .withRecipientKey(recipientKey)
        .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("PRIVACYGROUPID".getBytes()))
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void psvTxWithoutExecHash() {
    EncodedPayload.Builder.create()
        .withSenderKey(senderKey)
        .withCipherText(cipherText)
        .withCipherTextNonce(cipherTextNonce)
        .withRecipientBox(recipientBox)
        .withRecipientNonce(recipientNonce)
        .withPrivacyFlag(3)
        .withAffectedContractTransactions(affectedContractTransactionsRaw)
        .withRecipientKey(recipientKey)
        .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("PRIVACYGROUPID".getBytes()))
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void mandatoryRecipientsInvalid() {
    EncodedPayload.Builder.create()
        .withSenderKey(senderKey)
        .withCipherText(cipherText)
        .withCipherTextNonce(cipherTextNonce)
        .withRecipientBox(recipientBox)
        .withRecipientNonce(recipientNonce)
        .withPrivacyFlag(1)
        .withMandatoryRecipients(Set.of(PublicKey.from("KEY1".getBytes())))
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void mandatoryRecipientsInvalidNoData() {
    EncodedPayload.Builder.create()
        .withSenderKey(senderKey)
        .withCipherText(cipherText)
        .withCipherTextNonce(cipherTextNonce)
        .withRecipientBox(recipientBox)
        .withRecipientNonce(recipientNonce)
        .withPrivacyFlag(2)
        .build();
  }
}
