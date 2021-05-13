package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NoTesseraConfigfileOptionExceptionTest {

  @Test
  public void testDefaultConstrcutor() {
    assertThat(new NoTesseraConfigfileOptionException()).isNotNull();
  }
}
