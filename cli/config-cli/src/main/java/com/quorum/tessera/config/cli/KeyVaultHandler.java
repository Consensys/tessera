package com.quorum.tessera.config.cli;

import com.quorum.tessera.config.KeyVaultConfig;
import java.util.ServiceLoader;

public interface KeyVaultHandler {

  KeyVaultConfig handle(KeyVaultConfigOptions configOptions);

  static KeyVaultHandler create() {
    return ServiceLoader.load(KeyVaultHandler.class).findFirst().get();
  }
}
