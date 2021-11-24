package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class CBOREncoderTest {

  private final PayloadEncoder encoder = new CBOREncoder();

  private EncodedPayload standardPayload;

  @Before
  public void setUp() {
    standardPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("sender".getBytes()))
            .withCipherText("text".getBytes())
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withCipherTextNonce(new Nonce("cipherTextNonce".getBytes()))
            .withRecipientBoxes(List.of("box1".getBytes(), "box2".getBytes()))
            .withRecipientKeys(
                List.of(
                    PublicKey.from("recipient1".getBytes()),
                    PublicKey.from("recipient2".getBytes())))
            .build();
  }

  @Test
  public void testEncodeDecodeStandard() {

    final byte[] encoded = encoder.encode(standardPayload);

    final EncodedPayload result = encoder.decode(encoded);

    assertThat(result).isEqualTo(standardPayload);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(result.getAffectedContractTransactions()).isEmpty();
    assertThat(result.getExecHash()).isEmpty();
    assertThat(result.getMandatoryRecipients()).isEmpty();
    assertThat(result.getPrivacyGroupId()).isEmpty();
  }

  @Test
  public void testEncodeDecodePP() {

    EncodedPayload payload =
        EncodedPayload.Builder.from(standardPayload)
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("txHash1".getBytes()),
                    "securityHash1".getBytes(),
                    TxHash.from("txHash2".getBytes()),
                    "securityHash2".getBytes()))
            .build();

    final byte[] encoded = encoder.encode(payload);

    final EncodedPayload result = encoder.decode(encoded);

    assertThat(result).isEqualTo(payload);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(
            Map.of(
                TxHash.from("txHash1".getBytes()),
                SecurityHash.from("securityHash1".getBytes()),
                TxHash.from("txHash2".getBytes()),
                SecurityHash.from("securityHash2".getBytes())));
    assertThat(result.getExecHash()).isEmpty();
    assertThat(result.getMandatoryRecipients()).isEmpty();
    assertThat(result.getPrivacyGroupId()).isEmpty();
  }

  @Test
  public void testEncodeDecodePSV() {
    EncodedPayload payload =
        EncodedPayload.Builder.from(standardPayload)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("txHash1".getBytes()),
                    "securityHash1".getBytes(),
                    TxHash.from("txHash2".getBytes()),
                    "securityHash2".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encoded = encoder.encode(payload);

    final EncodedPayload result = encoder.decode(encoded);

    assertThat(result).isEqualTo(payload);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(
            Map.of(
                TxHash.from("txHash1".getBytes()),
                SecurityHash.from("securityHash1".getBytes()),
                TxHash.from("txHash2".getBytes()),
                SecurityHash.from("securityHash2".getBytes())));
    assertThat(result.getExecHash()).isEqualTo("execHash".getBytes());
    assertThat(result.getMandatoryRecipients()).isEmpty();
    assertThat(result.getPrivacyGroupId()).isEmpty();
  }

  @Test
  public void testEncodeDecodeMR() {
    EncodedPayload payload =
        EncodedPayload.Builder.from(standardPayload)
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("txHash1".getBytes()),
                    "securityHash1".getBytes(),
                    TxHash.from("txHash2".getBytes()),
                    "securityHash2".getBytes()))
            .withMandatoryRecipients(
                Set.of(
                    PublicKey.from("recipient1".getBytes()),
                    PublicKey.from("recipient2".getBytes())))
            .build();

    final byte[] encoded = encoder.encode(payload);

    final EncodedPayload result = encoder.decode(encoded);

    assertThat(result).isEqualTo(payload);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(
            Map.of(
                TxHash.from("txHash1".getBytes()),
                SecurityHash.from("securityHash1".getBytes()),
                TxHash.from("txHash2".getBytes()),
                SecurityHash.from("securityHash2".getBytes())));
    assertThat(result.getExecHash()).isEmpty();
    assertThat(result.getMandatoryRecipients())
        .isEqualTo(
            Set.of(
                PublicKey.from("recipient1".getBytes()), PublicKey.from("recipient2".getBytes())));
    assertThat(result.getPrivacyGroupId()).isEmpty();
  }

  @Test
  public void testEncodeDecodeWithPrivacyGroup() {

    PrivacyGroup.Id groupId = PrivacyGroup.Id.fromBytes("group".getBytes());

    EncodedPayload payload =
        EncodedPayload.Builder.from(standardPayload)
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withMandatoryRecipients(
                Set.of(
                    PublicKey.from("recipient1".getBytes()),
                    PublicKey.from("recipient2".getBytes())))
            .withPrivacyGroupId(groupId)
            .build();

    final byte[] encoded = encoder.encode(payload);

    final EncodedPayload result = encoder.decode(encoded);

    assertThat(result).isEqualTo(payload);
    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(result.getAffectedContractTransactions()).isEmpty();
    assertThat(result.getExecHash()).isEmpty();
    assertThat(result.getMandatoryRecipients())
        .isEqualTo(
            Set.of(
                PublicKey.from("recipient1".getBytes()), PublicKey.from("recipient2".getBytes())));
    assertThat(result.getPrivacyGroupId()).isPresent().get().isEqualTo(groupId);
  }

  @Test
  public void encodeError() {
    EncodedPayload payload = mock(EncodedPayload.class);
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> encoder.encode(payload))
        .withMessageContaining("Unable to encode payload");
  }

  @Test
  public void decodeError() {
    String invalid = "oWZzZW5kZXKA";
    byte[] raw = Base64.getDecoder().decode(invalid);
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> encoder.decode(raw))
        .withMessageContaining("Unable to decode payload data");
  }

  @Test
  public void codec() {
    assertThat(encoder.encodedPayloadCodec()).isEqualTo(EncodedPayloadCodec.CBOR);
  }
}
