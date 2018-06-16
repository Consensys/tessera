package com.github.nexus.keygen;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.nacl.NaclFacadeFactory;

public interface KeyGeneratorFactory {

    static KeyGenerator create(final Configuration configuration) {

        return new KeyGeneratorImpl(
            NaclFacadeFactory.newFactory().create(),
            configuration,
            KeyEncryptorFactory.create()
        );

    }

}
