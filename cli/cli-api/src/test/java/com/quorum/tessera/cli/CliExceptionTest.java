package com.quorum.tessera.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CliExceptionTest {

  @Test
  public void createWithMessage() {
    final String message = "OUCH";
    CliException cliException = new CliException(message);
    assertThat(cliException).hasMessage(message);
  }
}
