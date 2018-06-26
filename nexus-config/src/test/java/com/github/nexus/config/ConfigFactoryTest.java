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

    }

    @Test(expected = ConfigException.class)
    public void createFromSampleJaxbException() throws Exception {

        ConfigFactory configFactory = ConfigFactory.create();

        assertThat(configFactory).isExactlyInstanceOf(JaxbConfigFactory.class);

        InputStream inputStream = new ByteArrayInputStream("BANG".getBytes());

        configFactory.create(inputStream);

    }
}
