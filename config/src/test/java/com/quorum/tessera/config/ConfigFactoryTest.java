package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigFactoryTest {

    @Test
    public void create() {
        ConfigFactory configFactory = ConfigFactory.create();
        assertThat(configFactory).isNotNull().isExactlyInstanceOf(JaxbConfigFactory.class);

    }

}
