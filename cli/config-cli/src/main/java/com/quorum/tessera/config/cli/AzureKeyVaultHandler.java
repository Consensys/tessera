package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import java.util.Optional;

public class AzureKeyVaultHandler implements KeyVaultHandler {
  @Override
  public KeyVaultConfig handle(KeyVaultConfigOptions configOptions) {
    Optional<String> vaultUrl =
        Optional.ofNullable(configOptions).map(KeyVaultConfigOptions::getVaultUrl);
    return new AzureKeyVaultConfig(vaultUrl.orElse(null));
  }
}
