package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class PublicKeyCodec extends JsonCodec<PublicKey> {

    @Override
    public JsonObjectBuilder doEncode(PublicKey publicKey) throws Exception {
        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getKeyBytes());
        return Json.createObjectBuilder()
                .add("value", encodedKey);
    }

    @Override
    public PublicKey doDecode(JsonObject json) throws Exception {
        String publicKey = json.getString("value");
        byte[] data = Base64.getDecoder().decode(publicKey);
        return PublicKey.from(data);
    }

}
