package com.github.nexus.config.util;

import com.github.nexus.config.ConfigException;
import com.github.nexus.config.KeyDataConfig;
import com.github.nexus.config.PrivateKeyData;
import com.github.nexus.config.PrivateKeyType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import org.junit.Test;

import javax.xml.bind.MarshalException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class JaxbUtilTest {

    @Test
    public void unmarshalLocked() {

        final KeyDataConfig result = JaxbUtil.unmarshal(
                getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class
        );

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(result.getPrivateKeyData()).isNotNull();

        assertThat(result.getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(result.getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(result.getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(result.getArgonOptions()).isNotNull();
        assertThat(result.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(result.getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(result.getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(result.getArgonOptions().getMemory()).isEqualTo(1048576);
    }

    @Test
    public void marshallLocked() throws IOException {

        final KeyDataConfig input = new KeyDataConfig(
                new PrivateKeyData("VAL", null, null, null, null, null),
                PrivateKeyType.UNLOCKED
        );

        final String marshalled = JaxbUtil.marshalToString(input);

        try (Reader reader = new StringReader(marshalled)) {
            JsonObject result = Json.createReader(reader).readObject();

            assertThat(result).containsOnlyKeys("type", "data");
            assertThat(result.getString("type")).isEqualTo("unlocked");

            JsonObject jsonDataNode = result.getJsonObject("data");
            assertThat(jsonDataNode).containsOnlyKeys("bytes");
            assertThat(jsonDataNode.getString("bytes")).isEqualTo("VAL");
        }

    }

    @Test
    public void marshallingProducesError() {
        final Exception ex = new Exception();

        final Throwable throwable = catchThrowable(() -> JaxbUtil.marshalToString(ex));

        assertThat(throwable)
                .isInstanceOf(ConfigException.class)
                .hasCauseExactlyInstanceOf(MarshalException.class);
    }

    @Test
    public void marshallingOutputStream() throws Exception {
        final KeyDataConfig input = new KeyDataConfig(
                new PrivateKeyData("VAL", null, null, null, null, null),
                PrivateKeyType.UNLOCKED
        );

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            JaxbUtil.marshal(input, bout);

            JsonObject result = Json.createReader(new ByteArrayInputStream(bout.toByteArray())).readObject();

            assertThat(result).containsOnlyKeys("type", "data");
            assertThat(result.getString("type")).isEqualTo("unlocked");

            JsonObject jsonDataNode = result.getJsonObject("data");
            assertThat(jsonDataNode).containsOnlyKeys("bytes");
            assertThat(jsonDataNode.getString("bytes")).isEqualTo("VAL");

        }

    }

}
