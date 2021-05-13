package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivateKeyTypeTest {

  @Test
  public void testValues() {
    for (PrivateKeyType t : PrivateKeyType.values()) {
      assertThat(t).isNotNull();
      assertThat(PrivateKeyType.valueOf(t.name())).isSameAs(t);
    }
  }
}
