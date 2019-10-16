package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.Argon2;
import com.quorum.tessera.encryption.EncryptorFactory;

public interface KeyEncryptorFactory {

    static KeyEncryptor create() {
        final EncryptorFactory naclFactory = EncryptorFactory.newFactory();
        final Argon2 argon2 = Argon2.create();

        return new KeyEncryptorImpl(argon2, naclFactory.create());
    }
}
