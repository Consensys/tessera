package com.quorum.tessera.enclave.internal;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.enclave.encoder.LegacyControlPayloadEncoder;
import com.quorum.tessera.enclave.encoder.LegacyEncodedPayload;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class LegacyPayloadEncoderTest {

  private final LegacyPayloadEncoder payloadEncoder = new LegacyPayloadEncoder();

  private final LegacyControlPayloadEncoder legacyPayloadEncoder =
      new LegacyControlPayloadEncoder();

  // This tests a payload that has no data for the recipient list
  // NOT the case where the list is present but empty
  @Test
  public void decodeLegacyPayloadMissingRecipient() {
    final byte[] input =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75,
          -27, 55, 12, -1, 120, 13, 0, 86, 92, 52, 77, -4, 45, 0, 0, 0, 0, 0, 0, 0, 24, -115, -84,
          -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3, -73, -17, 6,
          124, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, -87, -102, 0, 95, -13, 48, 76, -115,
          -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95, -85, 78, -24, -30, 47, 65, 5, 88,
          38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109, 36, 49, -63, -123, 62,
          21, 67, -28, 0, 0, 0, 0, 0, 0, 0, 24, -63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36,
          44, -82, 15, -38, -19, 6, -101, 107, 110, -30, 95, 5
        };

    final byte[] senderKey =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75, -27, 55, 12, -1,
          120, 13, 0, 86, 92, 52, 77, -4, 45
        };
    final byte[] nonce =
        new byte[] {
          -115, -84, -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3,
          -73, -17, 6, 124
        };
    final byte[] recipientNonce =
        new byte[] {
          -63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36, 44, -82, 15, -38, -19, 6, -101, 107,
          110, -30, 95, 5
        };
    final byte[] recipient =
        new byte[] {
          -87, -102, 0, 95, -13, 48, 76, -115, -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95,
              -85, 78,
          -24, -30, 47, 65, 5, 88, 38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109,
              36, 49,
          -63, -123, 62, 21, 67, -28
        };

    final EncodedPayload output = payloadEncoder.decode(input);

    assertThat(output.getSenderKey()).isEqualTo(PublicKey.from(senderKey));
    assertThat(output.getCipherText()).containsExactly(cipherText);
    assertThat(output.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(output.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(output.getRecipientBoxes()).hasSize(1);
    assertThat(output.getRecipientBoxes().get(0).getData()).containsExactly(recipient);
    assertThat(output.getRecipientKeys()).isEmpty();
  }

  @Test
  public void decodeLegacyPayloadEmptyRecipients() {

    final byte[] input =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75,
          -27, 55, 12, -1, 120, 13, 0, 86, 92, 52, 77, -4, 45, 0, 0, 0, 0, 0, 0, 0, 24, -115, -84,
          -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3, -73, -17, 6,
          124, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, -87, -102, 0, 95, -13, 48, 76, -115,
          -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95, -85, 78, -24, -30, 47, 65, 5, 88,
          38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109, 36, 49, -63, -123, 62,
          21, 67, -28, 0, 0, 0, 0, 0, 0, 0, 24, -63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36,
          44, -82, 15, -38, -19, 6, -101, 107, 110, -30, 95, 5, 0, 0, 0, 0, 0, 0, 0, 0
        };

    final byte[] senderKey =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75, -27, 55, 12, -1,
          120, 13, 0, 86, 92, 52, 77, -4, 45
        };
    final byte[] nonce =
        new byte[] {
          -115, -84, -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3,
          -73, -17, 6, 124
        };
    final byte[] recipientNonce =
        new byte[] {
          -63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36, 44, -82, 15, -38, -19, 6, -101, 107,
          110, -30, 95, 5
        };
    final byte[] recipient =
        new byte[] {
          -87, -102, 0, 95, -13, 48, 76, -115, -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95,
              -85, 78,
          -24, -30, 47, 65, 5, 88, 38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109,
              36, 49,
          -63, -123, 62, 21, 67, -28
        };

    final EncodedPayload output = payloadEncoder.decode(input);

    assertThat(output.getSenderKey()).isEqualTo(PublicKey.from(senderKey));
    assertThat(output.getCipherText()).containsExactly(cipherText);
    assertThat(output.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(output.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(output.getRecipientBoxes()).hasSize(1);
    assertThat(output.getRecipientBoxes().get(0).getData()).containsExactly(recipient);
    assertThat(output.getRecipientKeys()).isEmpty();
    assertThat(output.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
  }

  @Test
  public void decodeLegacyPayloadWithRecipients() {

    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30,
          -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0,
          0, 0, 0, 0, 0, 0, 32, 53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38,
          43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys())
        .hasSize(2)
        .containsExactly(PublicKey.from(recipientKey1), PublicKey.from(recipientKey2));
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
  }

  @Test
  public void decodePartyProtectionPayloadNoRecipientNoAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys()).isEmpty();
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(payload.getAffectedContractTransactions()).isEmpty();
    assertThat(payload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void decodePartyProtectionPayloadNoRecipientWithAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 4, 116,
          101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 4, 116, 101, 115, 116
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys()).isEmpty();
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(payload.getAffectedContractTransactions()).hasSize(1);
    assertThat(payload.getAffectedContractTransactions().values().iterator().next().getData())
        .containsExactly("test".getBytes());
    assertThat(payload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void decodePartyProtectionPayloadWithRecipientsNoAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30,
          -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0,
          0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] recipientKey =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys()).hasSize(1).containsExactly(PublicKey.from(recipientKey));
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(payload.getAffectedContractTransactions()).isEmpty();
    assertThat(payload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void decodePartyProtectionPayloadWithRecipientsWithAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30,
          -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0,
          0, 0, 0, 0, 0, 0, 32, 53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38,
          43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0, 0, 0, 0, 0, 0,
          0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 4, 116, 101, 115, 116, 0, 0, 0, 0,
          0, 0, 0, 4, 116, 101, 115, 116
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys())
        .hasSize(2)
        .containsExactly(PublicKey.from(recipientKey1), PublicKey.from(recipientKey2));
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(payload.getAffectedContractTransactions()).hasSize(1);
    assertThat(payload.getAffectedContractTransactions().keySet())
        .containsExactly(new TxHash("test".getBytes()));
    assertThat(payload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void decodePsvPayloadNoRecipientsNoAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 101,
          120, 101, 99, 117, 116, 105, 111, 110, 72, 97, 115, 104
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] execHash = "executionHash".getBytes();

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys()).isEmpty();
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(payload.getAffectedContractTransactions()).isEmpty();
    assertThat(payload.getExecHash()).isEqualTo(execHash);
  }

  @Test
  public void decodePsvPayloadNoRecipientsWithAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 4, 116,
          101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 4, 116, 101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 13, 101,
          120, 101, 99, 117, 116, 105, 111, 110, 72, 97, 115, 104
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] execHash = "executionHash".getBytes();

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys()).isEmpty();
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(payload.getAffectedContractTransactions()).hasSize(1);
    assertThat(payload.getAffectedContractTransactions().values().iterator().next().getData())
        .containsExactly("test".getBytes());
    assertThat(payload.getExecHash()).isEqualTo(execHash);
  }

  @Test
  public void decodePsvPayloadWithRecipientsNoAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30,
          -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0,
          0, 0, 0, 0, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 101, 120, 101,
          99, 117, 116, 105, 111, 110, 72, 97, 115, 104
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] recipientKey =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] execHash = "executionHash".getBytes();

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys()).hasSize(1).containsExactly(PublicKey.from(recipientKey));
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(payload.getAffectedContractTransactions()).isEmpty();
    assertThat(payload.getExecHash()).isEqualTo(execHash);
  }

  @Test
  public void decodePsvPayloadWithRecipientsWithAffectedTx() {
    final byte[] encoded =
        new byte[] {
          0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78,
          30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0,
          0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116,
          97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117,
          -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53,
          -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81,
          -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14,
          -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61,
          -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113,
          118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0,
          0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30,
          -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0,
          0, 0, 0, 0, 0, 0, 32, 53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38,
          43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75, 0, 0, 0, 0, 0, 0,
          0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 4, 116, 101, 115, 116, 0, 0, 0, 0,
          0, 0, 0, 4, 116, 101, 115, 116, 0, 0, 0, 0, 0, 0, 0, 13, 101, 120, 101, 99, 117, 116, 105,
          111, 110, 72, 97, 115, 104
        };

    final byte[] sender =
        new byte[] {
          -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22,
          -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8
        };
    final byte[] cipherText =
        new byte[] {
          121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108,
          87, 124, -71, 87, -79, 15, -117, -23
        };
    final byte[] nonce =
        new byte[] {
          26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65,
          -83, 53, -77, -82, 104
        };
    final byte[] recipientBox =
        new byte[] {
          61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122,
              -7, -31,
          -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86,
              -78, 31, 86,
          61, -103, -92, -128, -74, 81
        };
    final byte[] recipientNonce =
        new byte[] {
          -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1,
          -86, 74, -25, -30, -88
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] execHash = "executionHash".getBytes();

    final EncodedPayload payload = payloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(PublicKey.from(sender));
    assertThat(payload.getCipherText()).containsExactly(cipherText);
    assertThat(payload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
    assertThat(payload.getRecipientBoxes()).hasSize(1);
    assertThat(payload.getRecipientBoxes().get(0).getData()).containsExactly(recipientBox);
    assertThat(payload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));
    assertThat(payload.getRecipientKeys())
        .hasSize(2)
        .containsExactly(PublicKey.from(recipientKey1), PublicKey.from(recipientKey2));
    assertThat(payload.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(payload.getAffectedContractTransactions()).hasSize(1);
    assertThat(payload.getAffectedContractTransactions().keySet())
        .containsExactly(new TxHash("test".getBytes()));
    assertThat(payload.getExecHash()).isEqualTo(execHash);
  }

  @Test
  public void encodeStandardPrivatePayloadNoRecipient() {

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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(emptyList())
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptyMap())
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(decodedPayload.getAffectedContractTransactions())
        .isEqualTo(originalPayload.getAffectedContractTransactions());
    assertThat(decodedPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void encodeStandardPrivatePayloadWithRecipients() {

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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey =
        new byte[] {
          68, -32, 25, 5, 107, 82, 105, -52, 87, 66, -77, -98, -36, 81, -128, -88, -112, -14, 38,
          49, 94, 61, 30, 92, 123, -124, -46, 35, 57, -119, -48, 23
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(singletonList(PublicKey.from(recipientKey)))
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash(new byte[0])
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(decodedPayload.getAffectedContractTransactions())
        .isEqualTo(originalPayload.getAffectedContractTransactions());
    assertThat(decodedPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void encodePartyProtectionPayloadNoRecipientsNoAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(emptyList())
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash(new byte[0])
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(decodedPayload.getAffectedContractTransactions()).isEmpty();
    assertThat(decodedPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void encodePartyProtectionPayloadNoRecipientsWithAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(emptyList())
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash(new byte[0])
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());

    assertThat(
            decodedPayload
                .getAffectedContractTransactions()
                .get(new TxHash("test".getBytes()))
                .getData())
        .isEqualTo("test".getBytes());
    assertThat(decodedPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void encodePartyProtectionPayloadWithRecipientsNoAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(PublicKey.from(recipientKey1));
    recipientList.add(PublicKey.from(recipientKey2));

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash(new byte[0])
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(decodedPayload.getAffectedContractTransactions()).isEmpty();
    assertThat(decodedPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void encodePartyProtectionPayloadWithRecipientsWithAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(PublicKey.from(recipientKey1));
    recipientList.add(PublicKey.from(recipientKey2));

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(
            decodedPayload
                .getAffectedContractTransactions()
                .get(new TxHash("test".getBytes()))
                .getData())
        .isEqualTo("test".getBytes());
    assertThat(decodedPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void encodePsvPayloadNoRecipientsNoAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(emptyList())
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(decodedPayload.getAffectedContractTransactions()).isEmpty();
    assertThat(decodedPayload.getExecHash()).isEqualTo(originalPayload.getExecHash());
  }

  @Test
  public void encodePsvPayloadNoRecipientsWithAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(emptyList())
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(
            decodedPayload
                .getAffectedContractTransactions()
                .get(new TxHash("test".getBytes()))
                .getData())
        .isEqualTo("test".getBytes());
    assertThat(decodedPayload.getExecHash()).isEqualTo(originalPayload.getExecHash());
  }

  @Test
  public void encodePsvPayloadWithRecipientsNoAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(PublicKey.from(recipientKey1));
    recipientList.add(PublicKey.from(recipientKey2));

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(emptyMap())
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(decodedPayload.getAffectedContractTransactions()).isEmpty();
    assertThat(decodedPayload.getExecHash()).isEqualTo(originalPayload.getExecHash());
  }

  @Test
  public void encodePsvPayloadWithRecipientsWithAffectedTx() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(PublicKey.from(recipientKey1));
    recipientList.add(PublicKey.from(recipientKey2));

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encodedResult = payloadEncoder.encode(originalPayload);
    final EncodedPayload decodedPayload = payloadEncoder.decode(encodedResult);

    assertThat(decodedPayload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(decodedPayload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(decodedPayload.getRecipientBoxes().size())
        .isEqualTo(originalPayload.getRecipientBoxes().size());
    assertThat(decodedPayload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(decodedPayload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
    assertThat(decodedPayload.getPrivacyMode()).isEqualTo(originalPayload.getPrivacyMode());
    assertThat(
            decodedPayload
                .getAffectedContractTransactions()
                .get(new TxHash("test".getBytes()))
                .getData())
        .isEqualTo("test".getBytes());
    assertThat(decodedPayload.getExecHash()).isEqualTo(originalPayload.getExecHash());
  }

  @Test
  public void encodeForSpecificRecipientNoPsv() {
    final String data =
        "00000000000000200542de47c272516862bae08c53f1cb034439a739184fe707208dd92817b2dc1a00000000000001796fe5bb76ae4d530a574acbe20cbb5094222eeaba32132fbda79c99e3d3df4e68466fe059f58c32c7ac55a5565e395c9394f608c741715e6bc60ca67d4e9fbcb842fef5e51dba7e537458fb5e201e67716751840662091feb0c029d95562e9929a13fff76f5bd27719a4d832100a04a4486c4f5c00ba9140b36a4900e2f29b1d29c9e8ff7baa9214f4cebc046f0840e1530b9fd774f0bd6da74635687b80251f4a97c4a9af799da572aeedcc2284f89574fa5a081aa328d7a9f33869b89141b2a005c2b4e58a07ecfa61700a08706edc7f30448353cbac7b836455fdf2742fcacf491d57731f938afb2a2de722b8e172a9e65a5979ec23239fc1a5adedfcd3f10d263239ab0fd75785945d798dc2ef8153c4d8dabc9d204fd98919d4e1183cbb0052bca3cd1a68f44d36472191eff7a86b3769f36189ee55a4aa4c212f369b297c82a7961199b00e6fbe7b9cec6ed53384ce025a0626921606bc3e28b7af44ccac85a18c534b56090fb4545693d1824c8929b42200a04a701420000000000000018499a2bedbac3eeaee6f400813382a5b5b7726ff5794974a2000000000000000100000000000000302badf5e765129f28e3d17ee318fba57d952d058cb93c8b407b95cc395bf86ab453c35ea3d8a88e38c459f5f002262795000000000000001887b36b4c47bdd2fddb2d1d8c94adfa7a4797d197cfdfeeac0000000000000001000000000000002044e019056b5269cc5742b39edc5180a890f226315e3d1e5c7b84d2233989d017";

    final byte[] decodedHex = Hex.decode(data);
    final EncodedPayload originalPayload = payloadEncoder.decode(decodedHex);

    final PublicKey recipientKey =
        PublicKey.from(Base64.getDecoder().decode("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc="));

    final EncodedPayload control = payloadEncoder.decode(decodedHex);

    final EncodedPayload result = payloadEncoder.forRecipient(originalPayload, recipientKey);

    assertThat(result).isNotNull();
    assertThat(result.getCipherText()).isEqualTo(control.getCipherText());
    assertThat(result.getSenderKey()).isEqualTo(control.getSenderKey());
    assertThat(result.getRecipientNonce()).isEqualTo(control.getRecipientNonce());
    assertThat(result.getCipherTextNonce()).isEqualTo(control.getCipherTextNonce());
    assertThat(result.getRecipientKeys()).hasSize(1).containsExactly(recipientKey);
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

    final EncodedPayload payload1 = payloadEncoder.forRecipient(originalPayload, recipient1);

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

    final EncodedPayload payload2 = payloadEncoder.forRecipient(originalPayload, recipient2);

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
    final String data =
        "00000000000000200542de47c272516862bae08c53f1cb034439a739184fe707208dd92817b2dc1a00000000000001796fe5bb76ae4d530a574acbe20cbb5094222eeaba32132fbda79c99e3d3df4e68466fe059f58c32c7ac55a5565e395c9394f608c741715e6bc60ca67d4e9fbcb842fef5e51dba7e537458fb5e201e67716751840662091feb0c029d95562e9929a13fff76f5bd27719a4d832100a04a4486c4f5c00ba9140b36a4900e2f29b1d29c9e8ff7baa9214f4cebc046f0840e1530b9fd774f0bd6da74635687b80251f4a97c4a9af799da572aeedcc2284f89574fa5a081aa328d7a9f33869b89141b2a005c2b4e58a07ecfa61700a08706edc7f30448353cbac7b836455fdf2742fcacf491d57731f938afb2a2de722b8e172a9e65a5979ec23239fc1a5adedfcd3f10d263239ab0fd75785945d798dc2ef8153c4d8dabc9d204fd98919d4e1183cbb0052bca3cd1a68f44d36472191eff7a86b3769f36189ee55a4aa4c212f369b297c82a7961199b00e6fbe7b9cec6ed53384ce025a0626921606bc3e28b7af44ccac85a18c534b56090fb4545693d1824c8929b42200a04a701420000000000000018499a2bedbac3eeaee6f400813382a5b5b7726ff5794974a2000000000000000100000000000000302badf5e765129f28e3d17ee318fba57d952d058cb93c8b407b95cc395bf86ab453c35ea3d8a88e38c459f5f002262795000000000000001887b36b4c47bdd2fddb2d1d8c94adfa7a4797d197cfdfeeac0000000000000001000000000000002044e019056b5269cc5742b39edc5180a890f226315e3d1e5c7b84d2233989d017";

    final byte[] decodedHex = Hex.decode(data);
    final EncodedPayload originalPayload = payloadEncoder.decode(decodedHex);

    final PublicKey recipientKey = mock(PublicKey.class);

    payloadEncoder.forRecipient(originalPayload, recipientKey);
  }

  @Test
  public void decodePayloadFromLegacyEncoderNoRecipient() {

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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    LegacyEncodedPayload legacyPayload =
        new LegacyEncodedPayload(
            PublicKey.from(sender),
            cipherText,
            new Nonce(nonce),
            singletonList(recipientBox),
            new Nonce(recipientNonce),
            emptyList());

    final byte[] encoded = legacyPayloadEncoder.encode(legacyPayload);

    EncodedPayload newPayload = payloadEncoder.decode(encoded);

    assertThat(newPayload.getSenderKey()).isEqualTo(legacyPayload.getSenderKey());
    assertThat(newPayload.getCipherText()).isEqualTo(legacyPayload.getCipherText());
    assertThat(newPayload.getCipherTextNonce()).isEqualTo(legacyPayload.getCipherTextNonce());
    assertThat(newPayload.getRecipientBoxes().get(0).getData())
        .isEqualTo(legacyPayload.getRecipientBoxes().get(0));
    assertThat(newPayload.getRecipientNonce()).isEqualTo(legacyPayload.getRecipientNonce());
    assertThat(newPayload.getRecipientKeys()).isEmpty();
    assertThat(newPayload.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(newPayload.getAffectedContractTransactions()).isEmpty();
    assertThat(newPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void decodePayloadFromLegacyEncoderWithRecipient() {

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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    LegacyEncodedPayload legacyPayload =
        new LegacyEncodedPayload(
            PublicKey.from(sender),
            cipherText,
            new Nonce(nonce),
            singletonList(recipientBox),
            new Nonce(recipientNonce),
            singletonList(PublicKey.from(recipientKey)));

    final byte[] encoded = legacyPayloadEncoder.encode(legacyPayload);

    final EncodedPayload newPayload = payloadEncoder.decode(encoded);

    assertThat(newPayload.getSenderKey()).isEqualTo(legacyPayload.getSenderKey());
    assertThat(newPayload.getCipherText()).isEqualTo(legacyPayload.getCipherText());
    assertThat(newPayload.getCipherTextNonce()).isEqualTo(legacyPayload.getCipherTextNonce());
    assertThat(newPayload.getRecipientBoxes().get(0).getData())
        .isEqualTo(legacyPayload.getRecipientBoxes().get(0));
    assertThat(newPayload.getRecipientNonce()).isEqualTo(legacyPayload.getRecipientNonce());
    assertThat(newPayload.getRecipientKeys()).containsExactly(PublicKey.from(recipientKey));
    assertThat(newPayload.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(newPayload.getAffectedContractTransactions()).isEmpty();
    assertThat(newPayload.getExecHash()).isNullOrEmpty();
  }

  @Test
  public void legacyEncoderShouldBeAbleToDecodeNewEncodedPayload() {

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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(PublicKey.from(recipientKey1));
    recipientList.add(PublicKey.from(recipientKey2));

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encoded = payloadEncoder.encode(originalPayload);

    final LegacyEncodedPayload payload = legacyPayloadEncoder.decode(encoded);

    assertThat(payload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(payload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(payload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(payload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0).getData());
    assertThat(payload.getRecipientNonce()).isEqualTo(originalPayload.getRecipientNonce());
    assertThat(payload.getRecipientKeys()).isEqualTo(originalPayload.getRecipientKeys());
  }

  @Test
  public void withRecipientForEncodedPayloadThatAlreadyHasRecipients() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };
    final byte[] recipientKey1 =
        new byte[] {
          35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100,
          3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };
    final byte[] recipientKey2 =
        new byte[] {
          53, -51, 72, -87, 21, -70, -41, 41, 9, -6, -29, -30, -67, 15, -38, 43, 36, 57, 90, 100, 3,
          80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75
        };

    List<PublicKey> recipientList = new ArrayList<>();
    recipientList.add(PublicKey.from(recipientKey1));
    recipientList.add(PublicKey.from(recipientKey2));

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(recipientList)
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final EncodedPayload payload =
        payloadEncoder.withRecipient(originalPayload, PublicKey.from("someKey".getBytes()));

    assertThat(payload).isSameAs(originalPayload);
  }

  @Test
  public void withRecipientForEncodedPayloadThatHasEmptyListRecipientKeys() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(emptyList())
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final EncodedPayload payload =
        payloadEncoder.withRecipient(originalPayload, PublicKey.from("someKey".getBytes()));

    assertThat(payload.getSenderKey()).isEqualTo(originalPayload.getSenderKey());
    assertThat(payload.getCipherText()).isEqualTo(originalPayload.getCipherText());
    assertThat(payload.getCipherTextNonce()).isEqualTo(originalPayload.getCipherTextNonce());
    assertThat(payload.getRecipientBoxes().get(0))
        .isEqualTo(originalPayload.getRecipientBoxes().get(0));
    assertThat(payload.getRecipientNonce()).isEqualTo(originalPayload.getRecipientNonce());
    assertThat(payload.getRecipientKeys()).containsExactly(PublicKey.from("someKey".getBytes()));
  }

  @Test
  public void withRecipientForEncodedPayloadThatHasEmptyArrayListRecipientKeys() {
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
    final byte[] recipientBox =
        new byte[] {
          -111, -41, -32, 59, -89, -69, -51, -27, 64, 74, -89, -63, -97, 54, 12, -10, -104, 111,
              -100, -98, 4,
          34, 67, 73, -57, -46, 15, 100, -21, -42, -14, -43, 72, 64, -127, -44, 113, -10, 82, 105,
              -81, 122,
          61, -50, 28, 108, -56, -92
        };
    final byte[] recipientNonce =
        new byte[] {
          -110, 45, 44, -76, 17, 23, -76, 0, -75, 112, 70, 97, 108, -70, -76, 32, 100, -46, -67,
          107, -89, 98, 64, -85
        };

    final EncodedPayload originalPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(sender))
            .withCipherText(cipherText)
            .withCipherTextNonce(new Nonce(nonce))
            .withRecipientBoxes(singletonList(recipientBox))
            .withRecipientNonce(new Nonce(recipientNonce))
            .withRecipientKeys(new ArrayList<>())
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                singletonMap(new TxHash("test".getBytes()), "test".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final EncodedPayload payload =
        payloadEncoder.withRecipient(originalPayload, PublicKey.from("someKey".getBytes()));

    assertThat(payload.getRecipientKeys()).containsExactly(PublicKey.from("someKey".getBytes()));
  }

  @Test
  public void encodeDecodePP() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(List.of(PublicKey.from("KEY1".getBytes())))
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isNullOrEmpty();
    assertThat(result.getPrivacyGroupId()).isNotPresent();
    assertThat(result.getMandatoryRecipients()).isEmpty();
  }

  @Test
  public void encodeDecodePPWithPrivacyGroupId() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(List.of(PublicKey.from("KEY1".getBytes())))
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PARTY_PROTECTION);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isNullOrEmpty();
    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyGroupId().get().getBytes()).isEqualTo("group".getBytes());
    assertThat(result.getMandatoryRecipients()).isEmpty();
  }

  @Test
  public void encodeDecodeMR() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(
                List.of(PublicKey.from("KEY1".getBytes()), PublicKey.from("KEY2".getBytes())))
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .withMandatoryRecipients(Set.of(PublicKey.from("KEY2".getBytes())))
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isNullOrEmpty();
    assertThat(result.getPrivacyGroupId()).isNotPresent();
    assertThat(result.getMandatoryRecipients()).isEqualTo(payload.getMandatoryRecipients());
  }

  @Test(expected = RuntimeException.class)
  public void encodeDecodeMREmptyList() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(
                List.of(PublicKey.from("KEY1".getBytes()), PublicKey.from("KEY2".getBytes())))
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .withMandatoryRecipients(emptySet())
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isNullOrEmpty();
    assertThat(result.getPrivacyGroupId()).isNotPresent();
    assertThat(result.getMandatoryRecipients()).isEmpty();
  }

  @Test
  public void encodeDecodeMRWithPrivacyGroupId() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(
                List.of(PublicKey.from("KEY1".getBytes()), PublicKey.from("KEY2".getBytes())))
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .withMandatoryRecipients(Set.of(PublicKey.from("KEY2".getBytes())))
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isNullOrEmpty();
    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyGroupId().get().getBytes()).isEqualTo("group".getBytes());
    assertThat(result.getMandatoryRecipients()).containsExactly(PublicKey.from("KEY2".getBytes()));
  }

  @Test
  public void encodeDecodePSV() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(
                List.of(PublicKey.from("KEY1".getBytes()), PublicKey.from("KEY2".getBytes())))
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isEqualTo("execHash".getBytes());
    assertThat(result.getPrivacyGroupId()).isNotPresent();
    assertThat(result.getMandatoryRecipients()).isEmpty();
  }

  @Test
  public void encodeDecodePSVWithPrivacyGroupId() {

    final EncodedPayload payload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from("SENDER".getBytes()))
            .withCipherText("CIPHER_TEXT".getBytes())
            .withCipherTextNonce(new Nonce("NONCE".getBytes()))
            .withRecipientBoxes(singletonList("recipientBox".getBytes()))
            .withRecipientNonce(new Nonce("recipientNonce".getBytes()))
            .withRecipientKeys(
                List.of(PublicKey.from("KEY1".getBytes()), PublicKey.from("KEY2".getBytes())))
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedContractTransactions(
                Map.of(
                    TxHash.from("hash1".getBytes()),
                    "1".getBytes(),
                    TxHash.from("hash2".getBytes()),
                    "2".getBytes()))
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("group".getBytes()))
            .withExecHash("execHash".getBytes())
            .build();

    final byte[] encoded = payloadEncoder.encode(payload);

    final EncodedPayload result = payloadEncoder.decode(encoded);

    assertThat(result.getSenderKey()).isEqualTo(payload.getSenderKey());
    assertThat(result.getCipherText()).isEqualTo(payload.getCipherText());
    assertThat(result.getCipherTextNonce()).isEqualTo(payload.getCipherTextNonce());
    assertThat(result.getRecipientBoxes()).isEqualTo(payload.getRecipientBoxes());
    assertThat(result.getRecipientNonce()).isEqualTo(payload.getRecipientNonce());
    assertThat(result.getRecipientKeys()).isEqualTo(payload.getRecipientKeys());

    assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(result.getAffectedContractTransactions())
        .isEqualTo(payload.getAffectedContractTransactions());
    assertThat(result.getExecHash()).isEqualTo("execHash".getBytes());
    assertThat(result.getPrivacyGroupId()).isPresent();
    assertThat(result.getPrivacyGroupId().get().getBytes()).isEqualTo("group".getBytes());
    assertThat(result.getMandatoryRecipients()).isEmpty();
  }

  @Test
  public void encodedPayloadCodec() {
    assertThat(payloadEncoder.encodedPayloadCodec()).isEqualTo(EncodedPayloadCodec.LEGACY);
  }
}
