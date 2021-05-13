package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NoTesseraCmdArgsExceptionTest {

  @Test
  public void defaultConstructor() {
    NoTesseraCmdArgsException exception = new NoTesseraCmdArgsException();
    assertThat(exception).isNotNull();
  }
}
