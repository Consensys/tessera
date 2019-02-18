package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.websockets.PublicKeyCodec;
import static com.quorum.tessera.enclave.websockets.JsonCodec.ENCODED_BY_KEY;
import com.quorum.tessera.encryption.PublicKey;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class PublicKeyCodecTest {

    private final PublicKeyCodec publicKeyCodec = new PublicKeyCodec();

    private final PublicKey publicKey = PublicKey.from("SOMEKEYDATA".getBytes());

    @Test
    public void encode() throws Exception {

        String resultData = publicKeyCodec.encode(publicKey);
        JsonObject result = Json.createReader(new StringReader(resultData)).readObject();

        assertThat(result).containsKeys(ENCODED_BY_KEY, "value");

        assertThat(result.getString(ENCODED_BY_KEY)).isEqualTo(PublicKeyCodec.class.getSimpleName());
        assertThat(result.getString("value")).isEqualTo("U09NRUtFWURBVEE=");

    }

    @Test
    public void decode() throws Exception {
        String input = "{\n"
                + "    \"value\": \"U09NRUtFWURBVEE=\"\n"
                + "}";

        PublicKey result = publicKeyCodec.decode(input);
        
        assertThat(result).isEqualTo(publicKey);
    }

}
