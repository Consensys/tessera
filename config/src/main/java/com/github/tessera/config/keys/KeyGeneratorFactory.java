package com.github.tessera.config.keys;


import com.github.tessera.nacl.NaclFacadeFactory;

public interface KeyGeneratorFactory {

    static KeyGenerator create() {

        return new KeyGeneratorImpl(
            NaclFacadeFactory.newFactory().create(),
            KeyEncryptorFactory.create()
        );

    }

}
