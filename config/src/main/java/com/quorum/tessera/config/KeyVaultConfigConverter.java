package com.quorum.tessera.config;

import java.util.Objects;
import java.util.Optional;

public interface KeyVaultConfigConverter {

  // AzureKeyVaultConfig is deprecated
  @Deprecated
  static DefaultKeyVaultConfig convert(AzureKeyVaultConfig azureKeyVaultConfig) {
    DefaultKeyVaultConfig config = new DefaultKeyVaultConfig();
    config.setKeyVaultType(azureKeyVaultConfig.getKeyVaultType());
    config.setProperty("url", azureKeyVaultConfig.getUrl());
    return config;
  }

  // HashicorpKeyVaultConfig is deprecated
  @Deprecated
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
