package com.github.tessera.transaction;

import com.github.tessera.nacl.Key;
import com.github.tessera.nacl.Nonce;
import com.github.tessera.transaction.model.EncodedPayload;
import com.github.tessera.transaction.model.EncodedPayloadWithRecipients;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import org.bouncycastle.util.encoders.Hex;

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

        final byte[] encoded = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 32, 
            -51, 40, -97, 78, 121, 47, -26, -66, 10, -21, -80, -22, -33, 78, 30, 85, -61, 56, 22, -100, 70, 124, 114, -34, -41, 36, -62, 6, 109, 63, -17, 8, 
            0, 0, 0, 0, 0, 0, 0, 28, 
            121, 84, 87, 65, -41, -37, 105, -73, 74, 33, -86, 53, 42, 39, 51, -116, 97, 108, 48, -108, 87, 124, -71, 87, -79, 15, -117, -23, 
            0, 0, 0, 0, 0, 0, 0, 24, 
            26, 117, -64, -69, -4, -127, 50, 16, -40, 92, -15, 52, 60, 119, -5, 117, -22, 121, 65, -83, 53, -77, -82, 104, //cipherText
            0, 0, 0, 0, 0, 0, 0, 1, 
            0, 0, 0, 0, 0, 0, 0, 48, 
            61, 89, -19, -11, 82, 81, -84, 119, -103, -104, 70, 34, 117, 1, 29, -100, -108, 112, 122, -7, -31, -36, 51, -12, 14, -120, -86, 49, -29, -41, 99, 106, -56, -35, -1, -117, -89, -45, -86, -78, 31, 86, 61, -103, -92, -128, -74, 81, 
            0, 0, 0, 0, 0, 0, 0, 24, 
            -92, 8, 108, -75, -70, 59, 77, 113, 118, 118, 118, -48, 13, -36, -116, 41, 127, 86, 1, -86, 74, -25, -30, -88, //recipientNonce
         //   0, 0, 0, 0, 0, 0, 0, 48, 
            0, 0, 0, 0, 0, 0, 0, 1, 
            0, 0, 0, 0, 0, 0, 0, 32, 
            35, -15, 27, -78, 21, -70, -41, 41, 9, -6, -92, -30, -67, 115, -38, 43, 36, 57, 90, 100, 3, 80, 87, -52, 52, 97, 1, -113, 97, 54, -71, 75 //recipientKey
        };
        
     
        
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

    @Test
    public void doStuff() {
        String str = "00000000000000200542de47c272516862bae08c53f1cb034439a739184fe707208dd92817b2dc1a00000000000001796fe5bb76ae4d530a574acbe20cbb5094222eeaba32132fbda79c99e3d3df4e68466fe059f58c32c7ac55a5565e395c9394f608c741715e6bc60ca67d4e9fbcb842fef5e51dba7e537458fb5e201e67716751840662091feb0c029d95562e9929a13fff76f5bd27719a4d832100a04a4486c4f5c00ba9140b36a4900e2f29b1d29c9e8ff7baa9214f4cebc046f0840e1530b9fd774f0bd6da74635687b80251f4a97c4a9af799da572aeedcc2284f89574fa5a081aa328d7a9f33869b89141b2a005c2b4e58a07ecfa61700a08706edc7f30448353cbac7b836455fdf2742fcacf491d57731f938afb2a2de722b8e172a9e65a5979ec23239fc1a5adedfcd3f10d263239ab0fd75785945d798dc2ef8153c4d8dabc9d204fd98919d4e1183cbb0052bca3cd1a68f44d36472191eff7a86b3769f36189ee55a4aa4c212f369b297c82a7961199b00e6fbe7b9cec6ed53384ce025a0626921606bc3e28b7af44ccac85a18c534b56090fb4545693d1824c8929b42200a04a701420000000000000018499a2bedbac3eeaee6f400813382a5b5b7726ff5794974a2000000000000000100000000000000302badf5e765129f28e3d17ee318fba57d952d058cb93c8b407b95cc395bf86ab453c35ea3d8a88e38c459f5f002262795000000000000001887b36b4c47bdd2fddb2d1d8c94adfa7a4797d197cfdfeeac0000000000000001000000000000002044e019056b5269cc5742b39edc5180a890f226315e3d1e5c7b84d2233989d017";

        byte[] data = Hex.decode(str);

        EncodedPayloadWithRecipients encodedPayloadWithRecipients = payloadEncoder.decodePayloadWithRecipients(data);

        assertThat(encodedPayloadWithRecipients).isNotNull();
        assertThat(encodedPayloadWithRecipients.getRecipientKeys()).hasSize(1);
    }

}
