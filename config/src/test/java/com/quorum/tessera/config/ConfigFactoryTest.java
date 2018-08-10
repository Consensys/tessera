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

        InputStream configInputStream = ElUtil.process(getClass()
                .getResourceAsStream("/sample.json"), params);

        Config config = configFactory.create(configInputStream, null);

        assertThat(config).isNotNull();
        assertThat(config.isUseWhiteList()).isFalse();
        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");
        assertThat(config.getPeers()).hasSize(2);
        assertThat(config.getKeys().getKeyData()).hasSize(1);

        final KeyData keyData = config.getKeys().getKeyData().get(0);

        assertThat(keyData.getConfig()).isNotNull();

        assertThat(keyData.getConfig().getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(keyData.getConfig().getPrivateKeyData()).isNotNull();

        assertThat(keyData.getConfig().getPrivateKeyData().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(keyData.getConfig().getPrivateKeyData().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(keyData.getConfig().getPrivateKeyData().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(keyData.getConfig().getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(keyData.getConfig().getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(keyData.getConfig().getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(keyData.getConfig().getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(keyData.getConfig().getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);
        
        
        Files.deleteIfExists(unixSocketPath);
    }

    @Test
    public void createFromSampleJaxbException() {

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        final InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        final Throwable throwable = catchThrowable(() -> configFactory.create(inputStream, null));
        assertThat(throwable).isInstanceOf(ConfigException.class);
    }

    @Test
    public void createFromKeyGenSample() throws Exception {

        final Path tempFolder = Files.createTempDirectory(UUID.randomUUID().toString()).toAbsolutePath();

        final String password = UUID.randomUUID().toString();

        final InputStream tempSystemIn = new ByteArrayInputStream(
                (password + System.lineSeparator() + password + System.lineSeparator()).getBytes()
        );

        final InputStream oldSystemIn = System.in;
        System.setIn(tempSystemIn);

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        Path unixSocketPath = Files.createTempFile(tempFolder, UUID.randomUUID().toString(), ".ipc");

        Map<String, Object> params = new HashMap<>();
        params.put("unixSocketPath", unixSocketPath.toString());

        InputStream configInputStream = ElUtil.process(getClass()
                .getResourceAsStream("/sample-private-keygen.json"), params);

        Config config = configFactory.create(configInputStream, null, tempFolder.toString());

        assertThat(config).isNotNull();
        assertThat(config.getKeys().getKeyData()).hasSize(1);

        KeyDataConfig keyDataConfig = config.getKeys().getKeyData().get(0).getConfig();

        assertThat(keyDataConfig.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(keyDataConfig.getPrivateKeyData()).isNotNull();

        assertThat(keyDataConfig.getPrivateKeyData().getSnonce()).isNotEmpty();
        assertThat(keyDataConfig.getPrivateKeyData().getAsalt()).isNotEmpty();
        assertThat(keyDataConfig.getPrivateKeyData().getSbox()).isNotEmpty();

        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("i");
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(keyDataConfig.getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);

        System.setIn(oldSystemIn);

        Files.deleteIfExists(unixSocketPath);

    }
}
