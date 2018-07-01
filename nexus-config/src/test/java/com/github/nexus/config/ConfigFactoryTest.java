package com.github.nexus.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ConfigFactoryTest {

    @Test
    public void createFromSample() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = getClass().getResourceAsStream("/sample.json");

        Config config = configFactory.create(inputStream);
        assertThat(config).isNotNull();

        assertThat(config.isUseWhiteList()).isFalse();

        assertThat(config.getJdbcConfig().getUsername()).isEqualTo("scott");

        assertThat(config.getPeers()).hasSize(2);

        assertThat(config.getKeys()).hasSize(1);

        PrivateKey privateKey = config.getKeys().stream()
                .map(KeyData::getPrivateKey).findAny().get();

        assertThat(privateKey.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(privateKey.getPrivateKeyData()).isNotNull();

        assertThat(privateKey.getPrivateKeyData().getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(privateKey.getPrivateKeyData().getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(privateKey.getPrivateKeyData().getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(privateKey.getPrivateKeyData().getArgonOptions()).isNotNull();
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(privateKey.getPrivateKeyData().getArgonOptions().getMemory()).isEqualTo(1048576);

    }

    @Test(expected = ConfigException.class)
    public void createFromSampleJaxbException() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        configFactory.create(inputStream);

    }
}
