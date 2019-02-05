package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

public class PublicKeySetCodec extends CodecAdapter<Set<PublicKey>> {

    @Override
    public JsonObjectBuilder doEncode(Set<PublicKey> object) throws EncodeException {

        Encoder base64Encoder = Base64.getEncoder();

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        object
                .stream()
                .map(PublicKey::getKeyBytes)
                .map(base64Encoder::encodeToString)
                .forEach(jsonArrayBuilder::add);

        return Json.createObjectBuilder()
                .add("keys", jsonArrayBuilder);
    }

    @Override
    public Set<PublicKey> doDecode(JsonObject json) throws DecodeException {

        Decoder base64Decoder = Base64.getDecoder();
        Set result = new HashSet();

        json.getJsonArray("keys")
                .stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .map(base64Decoder::decode)
                .map(PublicKey::from)
                .forEach(result::add);

        return result;

    }

}
