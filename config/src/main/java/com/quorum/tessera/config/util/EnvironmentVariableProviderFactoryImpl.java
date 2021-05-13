package com.quorum.tessera.config.util;

public class EnvironmentVariableProviderFactoryImpl implements EnvironmentVariableProviderFactory {

  @Override
  public EnvironmentVariableProvider create() {
    return new EnvironmentVariableProvider();
  }
}
