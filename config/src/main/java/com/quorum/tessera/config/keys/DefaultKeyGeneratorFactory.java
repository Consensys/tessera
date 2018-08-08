package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.util.PasswordReader;
import com.quorum.tessera.nacl.NaclFacadeFactory;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create() {
       return new KeyGeneratorImpl(
           NaclFacadeFactory.newFactory().create(),
           KeyEncryptorFactory.create(),
           new PasswordReader(System.console(), System.in)
       );
    }

}
