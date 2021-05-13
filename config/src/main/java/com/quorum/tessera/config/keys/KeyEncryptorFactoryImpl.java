package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.Argon2;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.EncryptorFactory;

public class KeyEncryptorFactoryImpl implements KeyEncryptorFactory {

  private final Argon2 argon2;

  public KeyEncryptorFactoryImpl() {
    this(Argon2.create());
  }

  protected KeyEncryptorFactoryImpl(Argon2 argon2) {
    this.argon2 = argon2;
  }

  @Override
  public KeyEncryptor create(EncryptorConfig encryptorConfig) {
    Encryptor encryptor =
        EncryptorFactory.newFactory(encryptorConfig.getType().name())
            .create(encryptorConfig.getProperties());
    return new KeyEncryptorImpl(argon2, encryptor);
  }
}
