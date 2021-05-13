package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.EncryptorConfig;

public interface KeyEncryptorFactory {

  static KeyEncryptorFactory newFactory() {
    return new KeyEncryptorFactoryImpl();
  }

  KeyEncryptor create(EncryptorConfig encryptorConfig);
}
