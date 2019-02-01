package com.jpmorgan.quorum.enclave.websockets;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public abstract class CodecAdapter<T> implements Encoder.Binary<T>, Decoder.Binary<T> {

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }

    
    
}
