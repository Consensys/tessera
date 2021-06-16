package com.quorum.tessera.cli.keypassresolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyPasswordResolverTest {

  @Test
  public void create() {
    assertThat(KeyPasswordResolver.create())
        .isNotNull()
        .isExactlyInstanceOf(CliKeyPasswordResolver.class);
  }
}
