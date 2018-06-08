package com.github.nexus.transaction;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.encryption.Nonce;
import com.github.nexus.transaction.model.EncodedPayload;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class PayloadEncoderTest {

    private PayloadEncoder payloadEncoder = new PayloadEncoderImpl();

    @Test
    public void validEncodedPayloadSerialisedToBytes() {

        final byte[] senderKeyInt = new byte[]{-51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8};
        final byte[] ciphertext = new byte[]{120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75, -27, 55, 12, -1, 120, 13, 0, 86, 92, 52, 77, -4, 45};
        final byte[] nonceInt = new byte[]{-115, -84, -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3, -73, -17, 6, 124};
        final byte[] recipientnonce = new byte[]{-63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36, 44, -82, 15, -38, -19, 6, -101, 107, 110, -30, 95, 5};
        final byte[] recipient = new byte[]{-87, -102, 0, 95, -13, 48, 76, -115, -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95, -85, 78, -24, -30, 47, 65, 5, 88, 38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109, 36, 49, -63, -123, 62, 21, 67, -28};


        final EncodedPayload encodedPayload = new EncodedPayload(
            new Key(senderKeyInt),
            ciphertext,
            new Nonce(nonceInt),
            singletonList(recipient),
            new Nonce(recipientnonce)
        );

        final byte[] result = new byte[]{0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0, 0, 0, 0, 0, 28, 120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75, -27, 55, 12, -1, 120, 13, 0, 86, 92, 52, 77, -4, 45, 0, 0, 0, 0, 0, 0, 0, 24, -115, -84, -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3, -73, -17, 6, 124, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, -87, -102, 0, 95, -13, 48, 76, -115, -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95, -85, 78, -24, -30, 47, 65, 5, 88, 38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109, 36, 49, -63, -123, 62, 21, 67, -28, 0, 0, 0, 0, 0, 0, 0, 24, -63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36, 44, -82, 15, -38, -19, 6, -101, 107, 110, -30, 95, 5};

        assertThat(payloadEncoder.encode(encodedPayload)).containsExactly(result);

    }

    @Test
    public void validBytesDecodeToEncodedPayload() {

        final byte[] input = new byte[]{0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0, 0, 0, 0, 0, 28, 120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75, -27, 55, 12, -1, 120, 13, 0, 86, 92, 52, 77, -4, 45, 0, 0, 0, 0, 0, 0, 0, 24, -115, -84, -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3, -73, -17, 6, 124, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, -87, -102, 0, 95, -13, 48, 76, -115, -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95, -85, 78, -24, -30, 47, 65, 5, 88, 38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109, 36, 49, -63, -123, 62, 21, 67, -28, 0, 0, 0, 0, 0, 0, 0, 24, -63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36, 44, -82, 15, -38, -19, 6, -101, 107, 110, -30, 95, 5};

        final byte[] senderKey = new byte[]{-51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8};
        final byte[] ciphertext = new byte[]{120, 111, 63, -100, 97, -12, -103, 20, 2, -48, 37, -86, -115, -112, -75, -27, 55, 12, -1, 120, 13, 0, 86, 92, 52, 77, -4, 45};
        final byte[] nonce = new byte[]{-115, -84, -58, 14, 82, 118, 4, -118, -53, 86, 3, 14, 112, 70, -4, 81, 121, 84, -24, -3, -73, -17, 6, 124};
        final byte[] recipientnonce = new byte[]{-63, 5, 86, 42, -85, -12, -36, 16, -108, 48, 26, 36, 44, -82, 15, -38, -19, 6, -101, 107, 110, -30, 95, 5};
        final byte[] recipient = new byte[]{-87, -102, 0, 95, -13, 48, 76, -115, -115, 62, 54, -55, -78, 125, -54, -34, -71, -11, -95, -85, 78, -24, -30, 47, 65, 5, 88, 38, -111, -12, -41, -97, 103, -60, -101, 43, -57, -68, 68, -109, 36, 49, -63, -123, 62, 21, 67, -28};

        final EncodedPayload encodedPayload = payloadEncoder.decode(input);

        assertThat(encodedPayload.getSenderKey()).isEqualTo(new Key(senderKey));
        assertThat(encodedPayload.getCipherText()).containsExactly(ciphertext);
        assertThat(encodedPayload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
        assertThat(encodedPayload.getRecipientNonce()).isEqualTo(new Nonce(recipientnonce));
        assertThat(encodedPayload.getRecipientBoxes()).hasSize(1);
        assertThat(encodedPayload.getRecipientBoxes().get(0)).containsExactly(recipient);

    }

    @Test
    public void validPayloadWithRecipientsEncodesToBytes() {
        final byte[] encoded = new byte[]{0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, -52, 0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0, 0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53, -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61, -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75};

        final byte[] sender = new byte[]{-51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8};
        final byte[] cipherText = new byte[]{121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23};
        final byte[] nonce = new byte[]{26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53, -77, -82, 104};
        final byte[] recipientBox = new byte[]{61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61, -103, -92, -128, -74, 81};
        final byte[] recipientNonce = new byte[]{-92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88};
        final byte[] recipientKey = new byte[]{35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75};

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = new EncodedPayloadWithRecipients(
            new EncodedPayload(
                new Key(sender),
                cipherText,
                new Nonce(nonce),
                singletonList(recipientBox),
                new Nonce(recipientNonce)
            ),
            singletonList(new Key(recipientKey))
        );

        final byte[] encodedResult = payloadEncoder.encode(encodedPayloadWithRecipients);

        assertThat(encodedResult).containsExactly(encoded);
    }

    @Test
    public void validBytesDecodeToEncodedPayloadWithRecipients() {
        final byte[] encoded = new byte[]{0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, -52, 0, 0, 0, 0, 0, 0, 0, 32, -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 0, 0, 0, 0, 0, 0, 0, 28, 121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 0, 0, 0, 0, 0, 0, 0, 24, 26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53, -77, -82, 104, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 48, 61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61, -103, -92, -128, -74, 81, 0, 0, 0, 0, 0, 0, 0, 24, -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, 0, 0, 0, 0, 0, 0, 0, 48, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32, 35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75};

        final byte[] sender = new byte[]{-51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8};
        final byte[] cipherText = new byte[]{121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23};
        final byte[] nonce = new byte[]{26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53, -77, -82, 104};
        final byte[] recipientBox = new byte[]{61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61, -103, -92, -128, -74, 81};
        final byte[] recipientNonce = new byte[]{-92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88};
        final byte[] recipientKey = new byte[]{35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75};

        final EncodedPayloadWithRecipients decoded = payloadEncoder.decodePayloadWithRecipients(encoded);
        final EncodedPayload decodedPayload = decoded.getEncodedPayload();

        assertThat(decodedPayload.getSenderKey()).isEqualTo(new Key(sender));
        assertThat(decodedPayload.getCipherText()).containsExactly(cipherText);
        assertThat(decodedPayload.getCipherTextNonce()).isEqualTo(new Nonce(nonce));
        assertThat(decodedPayload.getRecipientBoxes()).hasSize(1);
        assertThat(decodedPayload.getRecipientBoxes().get(0)).containsExactly(recipientBox);
        assertThat(decodedPayload.getRecipientNonce()).isEqualTo(new Nonce(recipientNonce));

        assertThat(decoded.getRecipientKeys()).hasSize(1);
        assertThat(decoded.getRecipientKeys().get(0)).isEqualTo(new Key(recipientKey));


    }


}
