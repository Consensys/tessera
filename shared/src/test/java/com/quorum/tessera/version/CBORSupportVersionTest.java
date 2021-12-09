package com.quorum.tessera.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CBORSupportVersionTest {

  private CBORSupportVersion version = new CBORSupportVersion();

  @Test
  public void getVersion() {
    assertThat(version.getVersion()).isEqualTo("5.0");
  }
}
