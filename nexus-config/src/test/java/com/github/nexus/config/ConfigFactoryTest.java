package com.github.nexus.config;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigFactoryTest {

    @Test
    public void createFromSample() {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = getClass().getResourceAsStream("/sample.json");

        Config config = configFactory.create(inputStream);
        assertThat(config).isNotNull();

        assertThat(config.isUseWhiteList()).isFalse();

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");

        assertThat(config.getPeers()).hasSize(2);

        assertThat(config.getKeys()).hasSize(1);

        KeyData keyData = config.getKeys().get(0);

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

    }

    @Test(expected = ConfigException.class)
    public void createFromSampleJaxbException() {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        configFactory.create(inputStream);

    }

    @Test
    public void createFromKeyGenSample() throws Exception {

        final ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        final Path keyFile = Paths.get(getClass().getResource("/lockedprivatekey.json").toURI());

        final Path configFile = Paths.get(getClass().getResource("/sample-private-keygen.json").toURI());

        Config config = configFactory.create(Files.newInputStream(configFile), Files.newInputStream(keyFile));

        assertThat(config).isNotNull();
        assertThat(config.getKeys()).hasSize(1);

        KeyDataConfig keyDataConfig = config.getKeys().get(0).getConfig();

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

    }
}
