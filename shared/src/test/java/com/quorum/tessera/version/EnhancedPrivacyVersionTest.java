package com.quorum.tessera.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EnhancedPrivacyVersionTest {

  private EnhancedPrivacyVersion version = new EnhancedPrivacyVersion();

  @Test
  public void getVersion() {
    assertThat(version.getVersion()).isEqualTo("v2");
  }
}
