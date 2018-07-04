package com.github.nexus.keyenc;


import com.github.nexus.nacl.NaclFacadeFactory;

public interface KeyGeneratorFactory {

    static KeyGenerator create() {

        return new KeyGeneratorImpl(
            NaclFacadeFactory.newFactory().create(),
            KeyEncryptorFactory.create()
        );

    }

}
