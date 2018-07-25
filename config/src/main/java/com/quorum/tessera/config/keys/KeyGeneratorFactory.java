package com.quorum.tessera.config.keys;


import com.quorum.tessera.nacl.NaclFacadeFactory;

public interface KeyGeneratorFactory {

    static KeyGenerator create() {

        return new KeyGeneratorImpl(
            NaclFacadeFactory.newFactory().create(),
            KeyEncryptorFactory.create()
        );

    }

}
