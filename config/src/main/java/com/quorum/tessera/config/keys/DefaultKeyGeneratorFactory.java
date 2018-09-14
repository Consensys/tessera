package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.nacl.NaclFacadeFactory;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create() {
       return new KeyGeneratorImpl(
           NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create()
       );
    }

}
