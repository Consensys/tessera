package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.encryption.PublicKey;
import java.util.Base64;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;


public class PublicKeyCodec implements Encoder.Text<PublicKey>, Decoder.Text<PublicKey>{

    @Override
    public String encode(PublicKey publicKey) throws EncodeException {
        return publicKey.encodeToBase64();
    }

    
    @Override
    public PublicKey decode(String publicKey) throws DecodeException {
        byte[] data = Base64.getDecoder().decode(publicKey);
        return PublicKey.from(data);
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
 
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

}
