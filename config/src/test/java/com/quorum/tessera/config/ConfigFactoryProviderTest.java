package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ServiceLoader;
import org.junit.Test;

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
