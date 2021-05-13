package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SslAuthenticationModeTest {

  @Test
  public void testValues() {
    for (SslAuthenticationMode t : SslAuthenticationMode.values()) {
      assertThat(t).isNotNull();
      assertThat(SslAuthenticationMode.valueOf(t.name())).isSameAs(t);
    }
  }
}
