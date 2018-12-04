package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import com.quorum.tessera.nacl.NaclFacadeFactory;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig) {

        if(keyVaultConfig != null) {
            final KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(keyVaultConfig.getKeyVaultType());

            final Config config = new Config();
            final KeyConfiguration keyConfiguration = new KeyConfiguration();

            if(keyVaultConfig.getKeyVaultType().equals(KeyVaultType.AZURE)) {
                keyConfiguration.setAzureKeyVaultConfig((AzureKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                KeyVaultClientFactory keyVaultClientFactory = KeyVaultClientFactory.getInstance(KeyVaultType.AZURE);

                final KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, new EnvironmentVariableProvider(), keyVaultClientFactory);

                return new AzureVaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);

            } else {
                keyConfiguration.setHashicorpKeyVaultConfig((HashicorpKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                KeyVaultClientFactory keyVaultClientFactory = KeyVaultClientFactory.getInstance(KeyVaultType.HASHICORP);

                final KeyVaultService keyVaultService = keyVaultServiceFactory.create(config, new EnvironmentVariableProvider(), keyVaultClientFactory);

                return new HashicorpVaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);
            }
        }

        return new FileKeyGenerator(
            NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create()
        );
    }
}
