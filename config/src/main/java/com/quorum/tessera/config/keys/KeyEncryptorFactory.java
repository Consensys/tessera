package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.Argon2;
import com.quorum.tessera.nacl.NaclFacadeFactory;

public interface KeyEncryptorFactory {

    static KeyEncryptor create() {
        final NaclFacadeFactory naclFactory = NaclFacadeFactory.newFactory();
        final Argon2 argon2 = Argon2.create();

        return new KeyEncryptorImpl(argon2, naclFactory.create());
    }

}
