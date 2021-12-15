package com.quorum.tessera.enclave;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
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
  public void fromPSV() {
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
  public void fromMR() {
    final EncodedPayload sample =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(List.of(recipientBox))
            .withRecipientNonce(recipientNonce)
            .withRecipientKeys(List.of(recipientKey))
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withMandatoryRecipients(Set.of(recipientKey))
            .build();

    EncodedPayload result = EncodedPayload.Builder.from(sample).build();

    assertThat(result).isNotSameAs(sample).isEqualTo(sample);
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

  @Test
  public void encodeForSpecificRecipientNoPsv() {

    final PublicKey key1 = mock(PublicKey.class);
    final PublicKey key2 = mock(PublicKey.class);
    final PublicKey key3 = mock(PublicKey.class);

    final byte[] box1 = "box1".getBytes();
    final byte[] box2 = "box2".getBytes();
    final byte[] box3 = "box3".getBytes();

    final EncodedPayload original =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBoxes(List.of(box1, box2, box3))
            .withRecipientNonce(recipientNonce)
            .withRecipientKeys(List.of(key1, key2, key3))
            .build();

    final EncodedPayload result = EncodedPayload.Builder.forRecipient(original, key2).build();

    assertThat(result).isNotNull();
    assertThat(result.getCipherText()).isEqualTo(original.getCipherText());
    assertThat(result.getSenderKey()).isEqualTo(original.getSenderKey());
    assertThat(result.getRecipientNonce()).isEqualTo(original.getRecipientNonce());
    assertThat(result.getCipherTextNonce()).isEqualTo(original.getCipherTextNonce());
    assertThat(result.getRecipientKeys()).hasSize(1).containsExactly(key2);
    assertThat(result.getRecipientBoxes()).hasSize(1).containsExactly(RecipientBox.from(box2));
    assertThat(result.getRecipientBoxes()).isNotEqualTo(original.getRecipientBoxes());
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);

    assertThat(result.getPrivacyGroupId()).isNotPresent();
  }

  @Test
  public void encodeForSpecificRecipientWithPsv() {
    final byte[] sender =
        new byte[] {
          5, 66, -34, 71, -62, 114, 81, 104, 98, -70, -32, -116, 83, -15, -53, 3, 68, 57, -89, 57,
          24, 79, -25, 7, 32, -115, -39, 40, 23, -78, -36, 26
        };
    final byte[] cipherText =
        new byte[] {
          -46, -26, -18, 127, 37, -2, -84, -56, -71, 26, 3, 102, -61, 38, -1, 37, 105, 2, 10, 86, 6,
          117, 69, 73, 91, 81, 68, 106, 23, 74, 12, 104, -63, 63, -119, 95, -16, -82, -34, 101, 89,
          38, -19, 8, 23, -70, 90, 5, -7, -15, 23, -8, -88, 47, 72, 105, -103, -34, 10, 109, -48,
          114, -127, -38, 41, 12, 3, 72, 113, -56, -90, -70, 124, -25, 127, 60, 100, 95, 127, 31,
          -72, -101, 26, -12, -9, 108, 54, 2, 124, 22, 55, 9, 123, 54, -16, 51, 28, -25, -102, -100,
          -23, 89, -15, 86, 22, -100, -63, -110, -2, -32, -1, 12, -116, 102, -43, 92, 2, 105, -78,
          -73, 111, -123, -59, -118, -32, 47, -63, 41, 72, -72, 35, -68, 45, 77, 110, -24, -113,
          -106, -31, -42, 13, -123, 54, 45, 83, -38, -57, 116, 107, -84, 22, -30, -49, 84, 39, 17,
          -20, -75, -122, -6, 73, -61, 70, -53, -65, -22, 13, 23, 43, -101, 23, 16, 31, -1, -19, -8,
          -94, -119, -28, -127, -101, 43, 31, -28, 16, -78, -86, 47, 42, 21, 115, 127, -81, 44, -33,
          -12, -74, -77, 111, 0, 121, 70, 67, 81, 74, 90, 116, -14, -75, 82, -110, -119, -23, 84,
          74, 61, -31, -66, -71, -106, 60, 127, -113, -26, 73, -50, -112, -45, 82, 37, -68, -49, 40,
          -73, -53, 85, -71, 82, 32, 117, 25, -81, -13, -30, -48, -118, -82, 125, -63, 1, -46, -115,
          -104, 32, 2, -1, -124, -88, -20, -77, 108, 123, 41, 78, 108, -88, 65, 84, 66, -40, 79,
          -118, 63, -109, -85, -52, 8, -97, -49, 87, -27, -63, 75, -45, 51, 7, 116, -68, 16, 89, 53,
          14, -121, 53, 38, -16, 122, -47, -110, -19, 72, 102, -81, 13, 13, -28, -103, 39, -26, 36,
          -15, -61, -91, -64, -99, 118, -34, -45, -119, 33, 57, 92, 119, 95, -17, 19, 50, 46, -119,
          88, -123, -49, -68, -105, 74, -15, 102, 74, -19, 29, 75, -114, -34, -54, -6, 111, 122, 2,
          55, 99, 58, -31, 123, 50, -84, -128, 71, 79, 19, -40, 92, 7, 75, -31, -113, -60, -8, 121,
          105, 91, -127, 69, 106, -49, -13, -91, -34
        };
    final byte[] nonce =
        new byte[] {
          -114, -128, 47, 49, 6, -71, -111, -76, -100, -16, 113, -126, 3, 107, 55, 1, 43, -6, -43,
          -104, -128, -125, -37, 31
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final PublicKey recipient1 = PublicKey.from("recipient".getBytes());
    final PublicKey recipient2 = PublicKey.from("anotherRecipient".getBytes());

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(recipient1);
    recipientList.add(recipient2);

    List<byte[]> recipientBoxes = new ArrayList<>();
    recipientBoxes.add("box".getBytes());
    recipientBoxes.add("anotherBox".getBytes());

    final PrivacyGroup.Id groupId = PrivacyGroup.Id.fromBytes("group".getBytes());

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(recipientBoxes)
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .withPrivacyGroupId(groupId)
            .build();

    final EncodedPayload payload1 =
        EncodedPayload.Builder.forRecipient(originalPayload, recipient1).build();

    assertThat(payload1).isNotNull();
    assertThat(payload1.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(payload1.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(payload1.getRecipientNonce()).isEqualTo(originalPayload.getRecipientNonce());
    assertThat(payload1.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(payload1.getRecipientKeys()).hasSize(2).containsExactly(recipient1, recipient2);
    assertThat(payload1.getRecipientBoxes()).isNotEqualTo(originalPayload.getRecipientBoxes());
    assertThat(payload1.getRecipientBoxes())
        .hasSize(1)
        .containsExactly(RecipientBox.from("box".getBytes()));
    assertThat(payload1.getPrivacyGroupId()).isPresent().get().isEqualTo(groupId);

    final EncodedPayload payload2 =
        EncodedPayload.Builder.forRecipient(originalPayload, recipient2).build();

    assertThat(payload2).isNotNull();
    assertThat(payload2.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(payload2.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(payload2.getRecipientNonce()).isEqualTo(originalPayload.getRecipientNonce());
    assertThat(payload2.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(payload2.getRecipientKeys()).hasSize(2).containsExactly(recipient2, recipient1);
    assertThat(payload2.getRecipientBoxes()).isNotEqualTo(originalPayload.getRecipientBoxes());
    assertThat(payload2.getRecipientBoxes())
        .hasSize(1)
        .containsExactly(RecipientBox.from("anotherBox".getBytes()));
    assertThat(payload1.getPrivacyGroupId()).isPresent().get().isEqualTo(groupId);
  }

  @Test(expected = InvalidRecipientException.class)
  public void encodeForSpecificRecipientNotContainedInPayload() {

    final EncodedPayload original =
        EncodedPayload.Builder.create()
            .withSenderKey(senderKey)
            .withCipherText(cipherText)
            .withCipherTextNonce(cipherTextNonce)
            .withRecipientBox(recipientBox)
            .withRecipientNonce(recipientNonce)
            .withRecipientKey(recipientKey)
            .build();

    final PublicKey recipientKey = mock(PublicKey.class);

    EncodedPayload.Builder.forRecipient(original, recipientKey);
  }
}
