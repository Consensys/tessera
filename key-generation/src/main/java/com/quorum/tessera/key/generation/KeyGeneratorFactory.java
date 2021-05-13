package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import java.util.ServiceLoader;

public interface KeyGeneratorFactory {

  KeyGenerator create(KeyVaultConfig keyVaultConfig, EncryptorConfig encryptorConfig);

  static KeyGeneratorFactory create() {
    return ServiceLoader.load(KeyGeneratorFactory.class).findFirst().get();
  }
}
