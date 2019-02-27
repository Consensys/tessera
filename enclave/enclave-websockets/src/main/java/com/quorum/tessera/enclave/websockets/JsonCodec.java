package com.quorum.tessera.enclave.websockets;

import java.io.StringReader;
import java.util.HashMap;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriterFactory;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Objects;
import javax.json.stream.JsonParser;

public abstract class JsonCodec<T> implements Decoder.Text<T>, Encoder.Text<T> {

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ENCODED_BY_KEY = "encodedBy";

    private JsonWriterFactory writerFactory = Json.createWriterFactory(new HashMap<String, Object>() {
        {
            put(JsonGenerator.PRETTY_PRINTING, logger.isDebugEnabled());
        }
    });

    @Override
    public final void init(EndpointConfig config) {
        logger.info("Init {}", config);
    }

    @Override
    public final void destroy() {
        logger.info("destroy");

    }

    @Override
    public final String encode(T object) throws EncodeException {

        try (Writer writer = new StringWriter()){
            logger.debug("Encoding {}", object);

            JsonObjectBuilder encoded = this.doEncode(object);

            JsonObject encodedObject = encoded
                    .add(ENCODED_BY_KEY, getClass().getSimpleName())
                    .build();

            try (JsonWriter w = writerFactory.createWriter(writer)){
                w.writeObject(encodedObject);
                String encodedString = writer.toString();
                logger.debug("Encoded {} to {}", object, encodedString);
                return encodedString;
            }
        } catch (Exception ex) {
            throw new EncodeException(object, "Encoding error", ex);
        }

    }

    @Override
    public final T decode(String s) throws DecodeException {

        logger.debug("Decoding {}", s);

        try (java.io.Reader reader = new StringReader(s)){
            JsonObject json = Json.createReader(reader).readObject();
            T decoded = this.doDecode(json);
            logger.debug("Decoded {} to {}", s, decoded);
            return decoded;
        } catch (Exception ex) {
            throw new DecodeException(s, "Decoding error", ex);
        }
    }

    protected abstract JsonObjectBuilder doEncode(T object) throws Exception;

    protected abstract T doDecode(JsonObject s) throws Exception;

    @Override
    public boolean willDecode(String s) {
        logger.info("Will decode {}", s);
        try (JsonParser parser = Json.createParser(new StringReader(s))){

            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();

                if (event != JsonParser.Event.KEY_NAME) {
                    continue;
                }
                String keyName = parser.getString();
                if (!Objects.equals(keyName, ENCODED_BY_KEY)) {
                    continue;
                }
                parser.next();
                String encodedBy = parser.getString();
                boolean willDecode = Objects.equals(encodedBy, getClass().getSimpleName());
                if (willDecode) {
                    logger.debug("willDecode: {}", s);
                }
                return willDecode;
            }
        }
        return false;
    }

}
