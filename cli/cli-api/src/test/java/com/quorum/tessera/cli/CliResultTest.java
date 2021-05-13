package com.quorum.tessera.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.Config;
import org.junit.Test;

public class CliResultTest {

  @Test
  public void isSuppressStartup() {
    boolean expected = false;
    CliResult result = new CliResult(0, expected, null);

    assertThat(result.isSuppressStartup()).isEqualTo(expected);
  }

  @Test
  public void getStatus() {
    int expected = 1;
    CliResult result = new CliResult(1, false, null);

    assertThat(result.getStatus()).isEqualTo(expected);
  }

  @Test
  public void getConfig() {
    Config config = new Config();
    CliResult result = new CliResult(1, false, config);

    assertThat(result.getConfig()).isNotEmpty();
    assertThat(result.getConfig().get()).isEqualTo(config);
  }
}
