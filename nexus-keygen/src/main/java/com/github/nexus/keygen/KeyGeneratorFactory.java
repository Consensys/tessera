package com.github.nexus.keygen;


import com.github.nexus.keyenc.KeyEncryptorFactory;
import com.github.nexus.nacl.NaclFacadeFactory;

public interface KeyGeneratorFactory {

    static KeyGenerator create() {

        return new KeyGeneratorImpl(
            NaclFacadeFactory.newFactory().create(),
            KeyEncryptorFactory.create()
        );

    }

}
