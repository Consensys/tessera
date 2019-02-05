package com.jpmorgan.quorum.enclave.websockets;

import java.io.StringReader;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
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

        JsonObjectBuilder encoded = this.doEncode(object);

        logger.info("Encoded {} to {}", object, encoded);

        return encoded.add("encodedBy",getClass().getSimpleName()).build().toString();

    }

    @Override
    public final T decode(String s) throws DecodeException {
        
        logger.info("Decoding {}", s);
        JsonObject json = Json.createReader(new StringReader(s)).readObject();
        T decoded = this.doDecode(json);
        logger.info("Decoded {} to {}", s, decoded);
        return decoded;
    }

    public abstract JsonObjectBuilder doEncode(T object) throws EncodeException;

    public abstract T doDecode(JsonObject s) throws DecodeException;

    @Override
    public final boolean willDecode(String s) {
        try (JsonParser parser = Json.createParser(new StringReader(s))){

            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();

                if (event != JsonParser.Event.KEY_NAME) {
                    continue;
                }
                String keyName = parser.getString();
                if (!Objects.equals(keyName, "encodedBy")) {
                    continue;
                }
                parser.next();
                String requestType = parser.getString();
                return Objects.equals(requestType, getClass().getName());
            }

        }
        return false;
    }

}
