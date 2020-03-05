package com.quorum.tessera.config;

import com.quorum.tessera.test.util.ElUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ConfigFactoryTest {

    @Test
    public void createFromSample() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path unixSocketPath = Files.createTempFile(UUID.randomUUID().toString(), ".ipc");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        InputStream configInputStream = ElUtil.process(getClass().getResourceAsStream("/sample.json"), params);

        Config config = configFactory.create(configInputStream);

        assertThat(config).isNotNull();
        assertThat(config.isUseWhiteList()).isFalse();
        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
        assertThat(config.getPeers()).hasSize(2);
        assertThat(config.getKeys().getKeyData()).hasSize(1);
        assertThat(config.getKeys().getKeyData().get(0)).isInstanceOf(KeyData.class);
    }

    @Test
    public void createFromSampleJaxbException() {

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        final InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        final Throwable throwable = catchThrowable(() -> configFactory.create(inputStream));
        assertThat(throwable).isInstanceOf(ConfigException.class);
    }

    @Test
    public void createFromKeyGenSample() throws Exception {

        final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString()).toAbsolutePath();

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path unixSocketPath = Files.createTempFile(tempFolder, UUID.randomUUID().toString(), ".ipc");

        Map<String, Object> params = singletonMap("unixSocketPath", unixSocketPath.toString());

        InputStream configInputStream =
                ElUtil.process(getClass().getResourceAsStream("/sample-private-keygen.json"), params);

        Config config = configFactory.create(configInputStream);

        assertThat(config).isNotNull();
    }
}
