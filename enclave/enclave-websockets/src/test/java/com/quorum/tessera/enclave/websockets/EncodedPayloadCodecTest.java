package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.websockets.EncodedPayloadCodec;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadBuilder;
import com.quorum.tessera.encryption.PublicKey;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import javax.json.Json;
import javax.json.JsonObject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class EncodedPayloadCodecTest {

    private final PublicKey senderKey = PublicKey.from("senderKey".getBytes());

    private final byte[] cipherText = "cipherText".getBytes();
    
    private final byte[] cipherTextNonce = "cipherTextNonce".getBytes();

    private final byte[] recipientNonce = "recipientNonce".getBytes();
    
    private final PublicKey recipientKey = PublicKey.from("recipientKey".getBytes());

    private final byte[] recipientBox = "recipientBox".getBytes();

    private EncodedPayloadCodec encodedPayloadCodec = new EncodedPayloadCodec();

    @Test
    public void encode() throws Exception {


        final EncodedPayload sample
                = EncodedPayloadBuilder.create()
                        .withSenderKey(senderKey)
                        .withCipherText(cipherText)
                        .withCipherTextNonce(cipherTextNonce)
                        .withRecipientBoxes(Arrays.asList(recipientBox))
                        .withRecipientNonce(recipientNonce)
                        .withRecipientKeys(recipientKey)
                        .build();

        String data = encodedPayloadCodec.encode(sample);

        JsonObject result = Json.createReader(new StringReader(data)).readObject();

        assertThat(result).containsOnlyKeys("encodedBy","cipherText", "cipherTextNonce", "recipientBoxes", "recipientKeys", "recipientNonce", "senderKey");

        assertThat(result.getString("cipherText"))
                .isEqualTo(Base64.getEncoder().encodeToString(cipherText));
        System.out.println(data);
    }

    @Test
    public void decode() throws Exception {
        String sample = "{\"cipherText\":\"Y2lwaGVyVGV4dA==\""
                + ",\"cipherTextNonce\":\"Y2lwaGVyVGV4dE5vbmNl\""
                + ",\"recipientBoxes\":[\"cmVjaXBpZW50Qm94\"]"
                + ",\"recipientKeys\":[\"cmVjaXBpZW50S2V5\"],"
                + "\"recipientNonce\":\"cmVjaXBpZW50Tm9uY2U=\","
                + "\"senderKey\":\"c2VuZGVyS2V5\"}";

        EncodedPayload result = encodedPayloadCodec.decode(sample);
        assertThat(result.getSenderKey()).isEqualTo(senderKey);
        assertThat(result.getCipherText()).isEqualTo("cipherText".getBytes());

        assertThat(result.getRecipientBoxes()).containsExactly(recipientBox);

        assertThat(result.getRecipientNonce().getNonceBytes()).isEqualTo(recipientNonce);
        assertThat(result.getCipherTextNonce().getNonceBytes()).isEqualTo(cipherTextNonce);
        assertThat(result.getRecipientKeys()).containsExactly(recipientKey);
    }

}
