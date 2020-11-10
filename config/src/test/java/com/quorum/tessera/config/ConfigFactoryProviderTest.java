package com.quorum.tessera.config;

import org.junit.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigFactoryProviderTest {

    @Test
    public void createConfigFactoryFromServiceLoader() {
        ConfigFactory configFactory = ServiceLoader.load(ConfigFactory.class).findFirst().get();
        assertThat(configFactory).isNotNull().isExactlyInstanceOf(JaxbConfigFactory.class);
    }

    @Test
    public void coverDefaultConstructorEvenIfNotNeeded() {
        assertThat(new ConfigFactoryProvider()).isNotNull();
    }

}
