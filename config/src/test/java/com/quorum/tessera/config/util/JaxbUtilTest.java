package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.validation.ConstraintViolationException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.transform.TransformerException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

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

    @Test
    public void marshallingProducesError() {
        final Exception ex = new Exception();

        OutputStream out = mock(OutputStream.class);
        final Throwable throwable = catchThrowable(() -> JaxbUtil.marshal(ex, out));

        assertThat(throwable)
                .isInstanceOf(ConfigException.class)
                .hasCauseExactlyInstanceOf(MarshalException.class);
    }

    @Test
    public void marshallNoValidationOutputStream() throws Exception {
        //This will fail bean validation
        final KeyDataConfig input = new KeyDataConfig(null, null);

        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            JaxbUtil.marshalWithNoValidation(input, bout);

            JsonObject result = Json.createReader(new ByteArrayInputStream(bout.toByteArray())).readObject();

            assertThat(result).isEmpty();

        }

    }

    @Test
    public void marshallingFailsBeanValidation() throws Exception {
        final KeyDataConfig input = new KeyDataConfig(null, null);

        try (OutputStream bout = new ByteArrayOutputStream()) {
            JaxbUtil.marshal(input, bout);
            failBecauseExceptionWasNotThrown(ConstraintViolationException.class);
        } catch (ConstraintViolationException ex) {
            assertThat(ex)
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Test
    public void marshalToString() {

        final KeyDataConfig input = new KeyDataConfig(
                new PrivateKeyData("VAL", null, null, null, null, null),
                PrivateKeyType.UNLOCKED
        );

        String resultData = JaxbUtil.marshalToString(input);

        JsonObject result = Json.createReader(new StringReader(resultData)).readObject();

        assertThat(result).containsOnlyKeys("type", "data");
        assertThat(result.getString("type")).isEqualTo("unlocked");

        JsonObject jsonDataNode = result.getJsonObject("data");
        assertThat(jsonDataNode).containsOnlyKeys("bytes");
        assertThat(jsonDataNode.getString("bytes")).isEqualTo("VAL");

    }

    @Test
    public void marshalDOntValidateString() {

        final KeyDataConfig input = new KeyDataConfig(
                null,
                null
        );

        String resultData = JaxbUtil.marshalToStringNoValidation(input);

        JsonObject result = Json.createReader(new StringReader(resultData)).readObject();

        assertThat(result).isEmpty();

    }

    @Test
    public void marshallingNOValidationProducesError() {
        final Exception ex = new Exception();

        OutputStream out = mock(OutputStream.class);
        final Throwable throwable = catchThrowable(() -> JaxbUtil.marshalWithNoValidation(ex, out));

        assertThat(throwable)
                .isInstanceOf(ConfigException.class)
                .hasCauseExactlyInstanceOf(MarshalException.class);
    }

    @Test
    public void unwrapConstraintViolationException() {

        ConstraintViolationException validationException
                = new ConstraintViolationException(Collections.emptySet());

        Throwable exception = new Exception(validationException);

        Optional<ConstraintViolationException> result
                = JaxbUtil.unwrapConstraintViolationException(exception);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(validationException);

    }

    @Test
    public void marshallingProducesNonJaxbException() {
        final KeyDataConfig input = new KeyDataConfig(
                new PrivateKeyData("VAL", null, null, null, null, null),
                PrivateKeyType.UNLOCKED
        );

        IOException exception = new IOException("What you talking about willis?");

        OutputStream out = mock(OutputStream.class, (iom) -> {
            throw exception;
        });
        final Throwable throwable = catchThrowable(() -> JaxbUtil.marshal(input, out));

        assertThat(throwable)
                .isInstanceOf(ConfigException.class)
                .hasCauseExactlyInstanceOf(javax.xml.bind.MarshalException.class);

    }

    @Test
    public void marshalMaskedConfig() throws Exception {

        final String expectedMaskValue = "*********";
        try (InputStream inputStream = getClass().getResourceAsStream("/mask-fixture.json")) {
            final Config config = JaxbUtil.unmarshal(inputStream, Config.class);

            try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                JaxbUtil.marshalMasked(config, bout);

                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bout.toByteArray())) {
                    JsonObject result = Json.createReader(byteArrayInputStream).readObject();

                    assertThat(result.getJsonObject("jdbc").getString("password")).isEqualTo(expectedMaskValue);

                    JsonObject sslConfig = result.getJsonObject("server")
                            .getJsonObject("sslConfig");

                    sslConfig.entrySet().stream()
                            .filter(entry -> entry.getKey().toLowerCase().contains("password"))
                            .forEach(entry -> {

                                JsonString v = (JsonString) entry.getValue();
                                assertThat(v.getString()).isEqualTo(expectedMaskValue);

                            });

                    assertThat(result.getJsonObject("keys").getJsonArray("keyData")
                            .getJsonObject(0).getString("privateKey"))
                            .isEqualTo(expectedMaskValue);

                }

            }
        }

    }

    
    @Test
    public void marshalMaskedConfigDontDisplayPrivateKeyIfFileIsPresent() throws Exception {

        final String expectedMaskValue = "*********";
        try (InputStream inputStream = getClass().getResourceAsStream("/mask-fixture-with-private-key-path.json")) {

            final Config config = JaxbUtil.unmarshal(inputStream, Config.class);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            JaxbUtil.marshalMasked(config, bout);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bout.toByteArray());
            JsonObject result = Json.createReader(byteArrayInputStream).readObject();

            assertThat(result.getJsonObject("jdbc").getString("password")).isEqualTo(expectedMaskValue);

            JsonObject sslConfig = result.getJsonObject("server").getJsonObject("sslConfig");

            sslConfig.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains("password"))
                    .forEach(entry -> {
                        JsonString v = (JsonString) entry.getValue();
                        assertThat(v.getString()).isEqualTo(expectedMaskValue);
                    });
        }

    }
    
    @Test(expected = ConfigException.class)
    public void marshalMaskedConfigThrowsJAXBException() throws Exception {
        Config config = mock(Config.class);
        OutputStream outputStream = mock(OutputStream.class, (iom) -> {
            throw new JAXBException("");
        });
        JaxbUtil.marshalMasked(config, outputStream);

    }

    @Test(expected = ConfigException.class)
    public void marshalMaskedConfigThrowsIOException() throws Exception {
        Config config = mock(Config.class);
        OutputStream outputStream = mock(OutputStream.class, (iom) -> {
            throw new IOException("");
        });
        JaxbUtil.marshalMasked(config, outputStream);

    }

    @Test(expected = ConfigException.class)
    public void marshalMaskedConfigThrowsTransformerException() throws Exception {
        Config config = mock(Config.class);
        OutputStream outputStream = mock(OutputStream.class, (iom) -> {
            throw new TransformerException("");
        });
        JaxbUtil.marshalMasked(config, outputStream);

    }

}
