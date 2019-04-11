package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;

public class PublicKeySetCodec extends JsonCodec<PublicKey[]> {

    @Override
    public JsonObjectBuilder doEncode(PublicKey[] object) throws Exception {

        Encoder base64Encoder = Base64.getEncoder();

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        Arrays.stream(object)
                .map(PublicKey::getKeyBytes)
                .map(base64Encoder::encodeToString)
                .forEach(jsonArrayBuilder::add);

        return Json.createObjectBuilder()
                .add("keys", jsonArrayBuilder);
    }

    @Override
    public PublicKey[] doDecode(JsonObject json) throws Exception {

        Decoder base64Decoder = Base64.getDecoder();
        JsonArray keys = json.getJsonArray("keys");
        PublicKey[] publicKeys = new PublicKey[keys.size()];
        for(int i = 0;i < keys.size();i++) {
          JsonString s = (JsonString) keys.get(i);
          byte[] keyData = base64Decoder.decode(s.getString());
          publicKeys[i] = PublicKey.from(keyData);
          
        }
        return publicKeys;

    }

}
