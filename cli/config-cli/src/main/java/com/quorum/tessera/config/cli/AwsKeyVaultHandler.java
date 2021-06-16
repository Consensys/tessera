package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import java.util.Optional;

public class AwsKeyVaultHandler implements KeyVaultHandler {
  @Override
  public KeyVaultConfig handle(KeyVaultConfigOptions configOptions) {
    DefaultKeyVaultConfig awsKeyVaultConfig = new DefaultKeyVaultConfig();
    awsKeyVaultConfig.setKeyVaultType(KeyVaultType.AWS);

    Optional.ofNullable(configOptions)
        .map(KeyVaultConfigOptions::getVaultUrl)
        .ifPresent(u -> awsKeyVaultConfig.setProperty("endpoint", u));

    return awsKeyVaultConfig;
  }
}
