package com.quorum.tessera.config;

import java.util.Objects;
import java.util.Optional;

public interface KeyVaultConfigConverter {

  /*
  TODO: Remove these when AzureKeyVaultConfig is removed
   */
  static DefaultKeyVaultConfig convert(AzureKeyVaultConfig azureKeyVaultConfig) {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(azureKeyVaultConfig.getKeyVaultType());
    config.setProperty("url", azureKeyVaultConfig.getUrl());
    return config;
  }

  /*
  TODO: Remove these when HashicorpKeyVaultConfig is removed
   */
  static DefaultKeyVaultConfig convert(HashicorpKeyVaultConfig hashicorpKeyVaultConfig) {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(hashicorpKeyVaultConfig.getKeyVaultType());
    config.setProperty("url", hashicorpKeyVaultConfig.getUrl());
    config.setProperty("approlePath", hashicorpKeyVaultConfig.getApprolePath());

    Optional.ofNullable(hashicorpKeyVaultConfig.getTlsKeyStorePath())
        .map(Objects::toString)
        .ifPresent(
            v -> {
              config.setProperty("tlsKeyStorePath", v);
            });

    Optional.ofNullable(hashicorpKeyVaultConfig.getTlsTrustStorePath())
        .map(Objects::toString)
        .ifPresent(
            v -> {
              config.setProperty("tlsTrustStorePath", v);
            });

    return config;
  }
}
