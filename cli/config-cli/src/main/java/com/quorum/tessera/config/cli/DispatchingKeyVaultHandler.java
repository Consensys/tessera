package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import java.util.Map;
import java.util.Optional;

public class DispatchingKeyVaultHandler implements KeyVaultHandler {

  private Map<KeyVaultType, ? extends KeyVaultHandler> lookup =
      Map.of(
          KeyVaultType.AZURE, new AzureKeyVaultHandler(),
          KeyVaultType.HASHICORP, new HashicorpKeyVaultHandler(),
          KeyVaultType.AWS, new AwsKeyVaultHandler());

  @Override
  public KeyVaultConfig handle(KeyVaultConfigOptions configOptions) {
    KeyVaultType keyVaultType =
        Optional.ofNullable(configOptions)
            .map(KeyVaultConfigOptions::getVaultType)
            .orElse(KeyVaultType.AWS);
    return lookup.get(keyVaultType).handle(configOptions);
  }
}
