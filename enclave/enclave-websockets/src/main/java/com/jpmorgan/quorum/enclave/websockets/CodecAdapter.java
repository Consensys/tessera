package com.jpmorgan.quorum.enclave.websockets;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CodecAdapter<T> implements Encoder.Text<T>, Decoder.Text<T> {

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public final void init(EndpointConfig config) {
    }

    @Override
    public final void destroy() {
    }

    @Override
    public final String encode(T object) throws EncodeException {
        
        logger.info("Encoding {}", object);

        String encoded = this.doEncode(object);

        logger.info("Encoded {} to {}", object, encoded);

        return encoded;

    }

    @Override
    public final T decode(String s) throws DecodeException {
        logger.info("Decoding {}", s);
        T decoded = this.doDecode(s);
        logger.info("Decoded {} to {}", s, decoded);
        return decoded;
    }

    public abstract String doEncode(T object) throws EncodeException;

    public abstract T doDecode(String s) throws DecodeException;

    @Override
    public boolean willDecode(String s) {
        return true;
    }

}
