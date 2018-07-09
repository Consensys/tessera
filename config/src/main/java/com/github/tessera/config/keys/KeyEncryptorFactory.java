package com.github.tessera.config.keys;

import com.github.tessera.argon2.Argon2;
import com.github.tessera.nacl.NaclFacadeFactory;

public interface KeyEncryptorFactory {

    static KeyEncryptor create() {
        final NaclFacadeFactory naclFactory = NaclFacadeFactory.newFactory();
        final Argon2 argon2 = Argon2.create();

        return new KeyEncryptorImpl(argon2, naclFactory.create());
    }

}
