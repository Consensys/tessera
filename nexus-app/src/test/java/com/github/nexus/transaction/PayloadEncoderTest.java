package com.github.nexus.transaction;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.encryption.Nonce;
import org.junit.Test;

import java.util.Collections;

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
            Collections.singletonList(recipient),
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

}
