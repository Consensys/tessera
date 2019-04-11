package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.model.PartyInfo;
import java.nio.ByteBuffer;
import java.util.Objects;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class PartyInfoCodec implements Decoder.Binary<PartyInfo>, Encoder.Binary<PartyInfo> {

    private PartyInfoParser partyInfoParser;

    public PartyInfoCodec() {
        this(PartyInfoParser.create());
    }

    public PartyInfoCodec(PartyInfoParser partyInfoParser) {
        this.partyInfoParser = Objects.requireNonNull(partyInfoParser);
    }

    @Override
    public ByteBuffer encode(PartyInfo partyInfo) throws EncodeException {
        byte[] data = partyInfoParser.to(partyInfo);
        return ByteBuffer.wrap(data);
    }
    
    @Override
    public PartyInfo decode(ByteBuffer buffer) throws DecodeException {
        return partyInfoParser.from(buffer.array());
    }

    @Override
    public boolean willDecode(ByteBuffer buffer) {
        return true;
    }
    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public void destroy() {
    }
}
