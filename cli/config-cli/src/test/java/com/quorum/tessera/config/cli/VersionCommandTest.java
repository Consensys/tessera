package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.cli.CliResult;
import org.junit.Test;

public class VersionCommandTest {

  @Test
  public void call() {
    VersionCommand cmd = new VersionCommand();

    CliResult result = cmd.call();

    CliResult want = new CliResult(0, true, null);
    assertThat(result).isEqualToComparingFieldByField(want);
  }
}
