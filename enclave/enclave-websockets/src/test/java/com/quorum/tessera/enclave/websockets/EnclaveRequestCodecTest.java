package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class EnclaveRequestCodecTest {

    private EnclaveRequestCodec enclaveRequestCodec = new EnclaveRequestCodec();

    @Test
    public void encodeEncyrptPayloadInvocation() throws Exception {

        PublicKey publicKey = PublicKey.from("PublicKey".getBytes());
        String expectedPublicKeyString = Base64.getEncoder().encodeToString(publicKey.getKeyBytes());

        EnclaveRequest enclaveRequest = EnclaveRequest.Builder.create()
                .withType(EnclaveRequestType.ENCRYPT_PAYLOAD)
                .withArg("ENYCYTPT_THIS".getBytes())
                .withArg(publicKey)
                .withArg(Arrays.asList(publicKey))
                .build();

        String resultData = enclaveRequestCodec.encode(enclaveRequest);

        JsonObject json = Optional.of(resultData)
                .map(StringReader::new)
                .map(Json::createReader)
                .map(JsonReader::readObject).get();

        assertThat(json.getString("type")).isEqualTo(EnclaveRequestType.ENCRYPT_PAYLOAD.name());

        JsonArray argsList = json.getJsonArray("args");

        assertThat(argsList).hasSize(3);
        assertThat(argsList.getString(0)).isEqualTo(Base64.getEncoder().encodeToString("ENYCYTPT_THIS".getBytes()));
        assertThat(argsList.getString(1)).isEqualTo(expectedPublicKeyString);
        assertThat(argsList.getJsonArray(2).getString(0)).isEqualTo(expectedPublicKeyString);

    }

    @Test
    public void decode() throws Exception {

        PublicKey publicKey = PublicKey.from("PublicKey".getBytes());

        String input = "{\n"
                + "    \"type\": \"ENCRYPT_PAYLOAD\",\n"
                + "    \"args\": [\n"
                + "        \"RU5ZQ1lUUFRfVEhJUw==\",\n"
                + "        \"UHVibGljS2V5\",\n"
                + "        [\n"
                + "            \"UHVibGljS2V5\"\n"
                + "        ]\n"
                + "    ]"
                + "}";

        EnclaveRequest result = enclaveRequestCodec.decode(input);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isSameAs(EnclaveRequestType.ENCRYPT_PAYLOAD);
        assertThat(result.getArgs()).hasSize(3);
        assertThat(result.getArgs().get(0)).isEqualTo("ENYCYTPT_THIS".getBytes());
        assertThat(result.getArgs().get(1)).isEqualTo(publicKey);
        assertThat(result.getArgs().get(2)).isInstanceOf(List.class);
        List keys = (List) result.getArgs().get(2);
        assertThat(keys).containsExactly(publicKey);
    }
    


}
