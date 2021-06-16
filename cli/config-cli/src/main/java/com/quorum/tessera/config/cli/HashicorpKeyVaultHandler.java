package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.HashicorpKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import java.util.Optional;

public class HashicorpKeyVaultHandler implements KeyVaultHandler {
  @Override
  public KeyVaultConfig handle(KeyVaultConfigOptions configOptions) {
    return Optional.ofNullable(configOptions)
        .map(
            c -> {
              return new HashicorpKeyVaultConfig(
                  c.getVaultUrl(),
                  c.getHashicorpApprolePath(),
                  c.getHashicorpTlsKeystore(),
                  c.getHashicorpTlsTruststore());
            })
        .orElseGet(() -> new HashicorpKeyVaultConfig());
  }
}
