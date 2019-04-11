package com.quorum.tessera.enclave.websockets;

import static com.quorum.tessera.enclave.websockets.JsonCodec.ENCODED_BY_KEY;
import com.quorum.tessera.encryption.PublicKey;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class PublicKeySetCodecTest {

    private PublicKeySetCodec publicKeySetCodec = new PublicKeySetCodec();

    private PublicKey publicKey = PublicKey.from("SOMEKEYDATA".getBytes());
    
    @Test
    public void encode() throws Exception {


        PublicKey[] keys = {publicKey};

        String resultData = publicKeySetCodec.encode(keys);

        JsonObject result = Json.createReader(new StringReader(resultData)).readObject();

        assertThat(result).isNotNull();

        assertThat(result).containsKeys(ENCODED_BY_KEY, "keys");
        assertThat(result.getString(ENCODED_BY_KEY)).isEqualTo(PublicKeySetCodec.class.getSimpleName());
        assertThat(result.getJsonArray("keys")).hasSize(1);
        assertThat(result.getJsonArray("keys").getString(0)).isEqualTo("U09NRUtFWURBVEE=");

    }

    @Test
    public void decode() throws Exception {

        JsonObject input = Json.createObjectBuilder()
                .add("keys", Json.createArrayBuilder()
                        .add("U09NRUtFWURBVEE=")).build();

        PublicKey[] result = publicKeySetCodec.decode(input.toString());
        assertThat(result).containsOnly(publicKey);
    }

}
