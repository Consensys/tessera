package com.quorum.tessera.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BaseVersionTest {

  private BaseVersion apiVersion = new BaseVersion();

  @Test
  public void getVersion() {
    assertThat(apiVersion.getVersion()).isEqualTo("v1");
  }
}
