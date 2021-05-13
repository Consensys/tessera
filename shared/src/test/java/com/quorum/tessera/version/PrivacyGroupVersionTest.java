package com.quorum.tessera.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivacyGroupVersionTest {

  private PrivacyGroupVersion version = new PrivacyGroupVersion();

  @Test
  public void getVersion() {
    assertThat(version.getVersion()).isEqualTo("3.0");
  }
}
