package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.internal.JaxbConfigFactory;
import org.junit.Test;

public class ConfigFactoryTest {

  @Test
  public void create() {
    ConfigFactory configFactory = ConfigFactory.create();
    assertThat(configFactory).isNotNull().isExactlyInstanceOf(JaxbConfigFactory.class);
    assertThat(configFactory.getConfig()).isNull();
  }

  @Test
  public void store() {
    Config config = mock(Config.class);
    ConfigFactory configFactory = ConfigFactory.create();
    configFactory.store(config);
    assertThat(configFactory.getConfig()).isSameAs(config);
  }
}
