package com.quorum.tessera.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CliTypeTest {

  @Test
  public void values() {
    for (CliType t : CliType.values()) {
      assertThat(t).isNotNull();
      assertThat(CliType.valueOf(t.name())).isSameAs(t);
    }
  }
}
