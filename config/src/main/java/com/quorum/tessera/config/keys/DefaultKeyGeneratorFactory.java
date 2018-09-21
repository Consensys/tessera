package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.nacl.NaclFacadeFactory;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(boolean hasKeyVaultOptions) {
        if(hasKeyVaultOptions) {
            return new VaultKeyGenerator(NaclFacadeFactory.newFactory().create());
        } else {
            return new KeyGeneratorImpl(
                NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create()
            );
        }
    }
}
