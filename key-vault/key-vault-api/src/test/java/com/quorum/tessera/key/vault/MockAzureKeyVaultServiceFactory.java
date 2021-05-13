package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;

public class MockAzureKeyVaultServiceFactory implements KeyVaultServiceFactory {
  @Override
  public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
    throw new UnsupportedOperationException(
        "This mock object's method is not expected to be called");
  }

  @Override
  public KeyVaultType getType() {
    return KeyVaultType.AZURE;
  }
}
