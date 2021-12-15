package com.quorum.tessera.version;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ApiVersionTest {

  @Test
  public void create() {
    assertThat(ApiVersion.versions())
        .containsExactlyInAnyOrder("v1", "v2", "2.1", "3.0", "4.0", "5.0");
  }
}
