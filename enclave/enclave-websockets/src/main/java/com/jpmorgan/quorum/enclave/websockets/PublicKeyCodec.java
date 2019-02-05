package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;

public class PublicKeyCodec extends CodecAdapter<PublicKey> {

    @Override
    public String doEncode(PublicKey publicKey) throws EncodeException {
        return Base64.getEncoder().encodeToString(publicKey.getKeyBytes());
    }

    @Override
    public PublicKey doDecode(String publicKey) throws DecodeException {
        byte[] data = Base64.getDecoder().decode(publicKey);
        return PublicKey.from(data);
    }

    @Override
    public boolean willDecode(String s) {
        return !s.startsWith("[");
    }

}
