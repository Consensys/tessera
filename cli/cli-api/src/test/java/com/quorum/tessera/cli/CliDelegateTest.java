package com.quorum.tessera.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import org.junit.Test;

public class CliDelegateTest {

  private final CliDelegate instance = CliDelegate.INSTANCE;

  @Test
  public void createInstance() {
    assertThat(CliDelegate.instance()).isSameAs(instance);
  }

  @Test(expected = IllegalStateException.class)
  public void fetchConfigBeforeSet() {
    instance.getConfig();
  }

  @Test
  public void fetchConfigAfterSet() {
    Config config = new Config();

    instance.setConfig(config);

    assertThat(instance.getConfig()).isEqualTo(config);
  }
}
