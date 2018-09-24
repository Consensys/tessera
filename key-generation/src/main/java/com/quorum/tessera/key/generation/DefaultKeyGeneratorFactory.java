package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.key.vault.AzureKeyVaultClientDelegate;
import com.quorum.tessera.key.vault.AzureKeyVaultClientFactory;
import com.quorum.tessera.key.vault.AzureKeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.nacl.NaclFacadeFactory;

import java.util.concurrent.Executors;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig) {
        if(keyVaultConfig != null) {
            //TODO improve this
            final KeyVaultService keyVaultService = new AzureKeyVaultService(
                new KeyVaultConfig(keyVaultConfig.getUrl()),
                new AzureKeyVaultClientDelegate(
                    new AzureKeyVaultClientFactory(
                        Executors.newFixedThreadPool(1)
                    ).getAuthenticatedClient()
                )
            );

            return new VaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);
        } else {
            return new KeyGeneratorImpl(
                NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create()
            );
        }
    }
}
