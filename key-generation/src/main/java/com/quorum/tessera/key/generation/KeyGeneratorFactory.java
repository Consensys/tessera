package com.quorum.tessera.key.generation;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.KeyVaultConfig;

public interface KeyGeneratorFactory {

  KeyGenerator create(KeyVaultConfig keyVaultConfig, EncryptorConfig encryptorConfig);

  static KeyGeneratorFactory newFactory() {
    return ServiceLoaderUtil.load(KeyGeneratorFactory.class)
        .orElse(new DefaultKeyGeneratorFactory());
  }
}
