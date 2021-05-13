package com.quorum.tessera.config.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EnvironmentVariableProviderFactoryImplTest {

  @Test
  public void create() {
    EnvironmentVariableProviderFactoryImpl factory = new EnvironmentVariableProviderFactoryImpl();

    assertThat(factory.create()).isExactlyInstanceOf(EnvironmentVariableProvider.class);
  }
}
