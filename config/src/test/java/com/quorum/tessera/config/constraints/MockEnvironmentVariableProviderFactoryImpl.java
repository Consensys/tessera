package com.quorum.tessera.config.constraints;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;

public class MockEnvironmentVariableProviderFactoryImpl
    implements EnvironmentVariableProviderFactory {

  private static final EnvironmentVariableProvider envVarProvider =
      mock(EnvironmentVariableProvider.class);

  @Override
  public EnvironmentVariableProvider create() {
    return envVarProvider;
  }
}
