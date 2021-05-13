package com.quorum.tessera.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class VersionTest {

  @Test
  public void getVersion() {
    String version = Version.getVersion();
    assertThat(version).isEqualTo(MockVersion.VERSION);
  }

  @Test
  public void getDefaultVersion() {
    String version = new Version() {}.version();
    assertThat(version).isNull();
  }
}
