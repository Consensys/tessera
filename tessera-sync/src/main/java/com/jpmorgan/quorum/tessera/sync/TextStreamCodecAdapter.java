package com.jpmorgan.quorum.tessera.sync;

import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public abstract class TextStreamCodecAdapter<T> implements Decoder.TextStream<T>, Encoder.TextStream<T> {

    @Override
    public void init(EndpointConfig config) {}

    @Override
    public void destroy() {}
}
