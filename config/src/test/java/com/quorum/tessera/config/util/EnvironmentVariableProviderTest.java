package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class EnvironmentVariableProviderTest {

  private EnvironmentVariableProvider provider;

  @Before
  public void setUp() {
    this.provider = new EnvironmentVariableProvider();
  }

  @Test
  public void getEnv() {
    // returns null as env variables not set in test environment
    assertThat(provider.getEnv("env")).isNull();
  }

  @Test
  public void getEnvAsCharArray() {
    // returns null as env variables not set in test environment
    assertThat(provider.getEnvAsCharArray("env")).isNull();
  }

  @Test
  public void hasEnv() {
    // returns false as env variables not set in test environment
    assertThat(provider.hasEnv("env")).isFalse();
  }
}
