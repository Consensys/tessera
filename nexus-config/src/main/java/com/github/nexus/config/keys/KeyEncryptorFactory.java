package com.github.nexus.config.keys;

import com.github.nexus.argon2.Argon2;
import com.github.nexus.nacl.NaclFacadeFactory;

public interface KeyEncryptorFactory {

    static KeyEncryptor create() {
        final NaclFacadeFactory naclFactory = NaclFacadeFactory.newFactory();
        final Argon2 argon2 = Argon2.create();

        return new KeyEncryptorImpl(argon2, naclFactory.create());
    }

}
