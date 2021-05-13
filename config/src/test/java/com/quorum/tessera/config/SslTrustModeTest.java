package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SslTrustModeTest {

  @Test
  public void testValues() {
    for (SslTrustMode t : SslTrustMode.values()) {
      assertThat(t).isNotNull();
      assertThat(SslTrustMode.valueOf(t.name())).isSameAs(t);
    }
  }
}
