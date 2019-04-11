package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.websockets.JsonCodec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParsingException;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class JsonCodecTest {

    private JsonCodec jsonCodec = new MockJsonCodec();

    @Test
    public void initAndDestroy() {
        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        jsonCodec.init(endpointConfig);
        jsonCodec.destroy();
        verifyZeroInteractions(endpointConfig);
    }

    @Test
    public void willDecode() {
        boolean result = jsonCodec.willDecode("{\"encodedBy\":\"MockJsonCodec\"}");

        assertThat(result).isTrue();

    }

    @Test
    public void willDecodeMissingEncodedByKey() {
        boolean result = jsonCodec.willDecode("{\"value\":\"Some value\"}");

        assertThat(result).isFalse();

    }

    @Test
    public void willNotDecode() {
        boolean result = jsonCodec.willDecode("{\"encodedBy\":\"BogusCodec\"}");

        assertThat(result).isFalse();
    }

    @Test(expected = JsonParsingException.class)
    public void willNotEncodeInvalidJson() {
        jsonCodec.willDecode("}");
    }

    @Test(expected = DecodeException.class)
    public void doDecodeException() throws Exception {
        JsonCodec codec = new JsonCodec() {
            @Override
            protected JsonObjectBuilder doEncode(Object object) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected Object doDecode(JsonObject s) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };

        codec.decode("{}");

    }

    @Test(expected = EncodeException.class)
    public void doEncodeException() throws Exception {
        JsonCodec codec = new JsonCodec() {
            @Override
            protected JsonObjectBuilder doEncode(Object object) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected Object doDecode(JsonObject s) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

        };

        codec.encode("{}");

    }

    static class MockJsonCodec extends JsonCodec<MockPayload> {

        @Override
        protected JsonObjectBuilder doEncode(MockPayload object) throws Exception {
            return Json.createObjectBuilder().add("value", "Some value");
        }

        @Override
        protected MockPayload doDecode(JsonObject s) throws Exception {
            return new MockPayload(s.getString("value"));
        }
    }

    static class MockPayload {

        private String value;

        MockPayload(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

}
