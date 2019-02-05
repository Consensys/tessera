package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

public class PublicKeyCodec extends CodecAdapter<PublicKey> {

    @Override
    public JsonObjectBuilder doEncode(PublicKey publicKey) throws EncodeException {

        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getKeyBytes());
        return Json.createObjectBuilder()
                .add("value", encodedKey);

    }

    @Override
    public PublicKey doDecode(JsonObject json) throws DecodeException {
        String publicKey = json.getString("value");
        byte[] data = Base64.getDecoder().decode(publicKey);
        return PublicKey.from(data);
    }

}
