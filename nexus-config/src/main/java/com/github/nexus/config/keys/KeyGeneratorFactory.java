package com.github.nexus.config.keys;


import com.github.nexus.nacl.NaclFacadeFactory;

public interface KeyGeneratorFactory {

    static KeyGenerator create() {

        return new KeyGeneratorImpl(
            NaclFacadeFactory.newFactory().create(),
            KeyEncryptorFactory.create()
        );

    }

}
