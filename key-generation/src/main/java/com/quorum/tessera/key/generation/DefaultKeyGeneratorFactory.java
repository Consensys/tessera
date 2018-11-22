package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.keypairs.KeyPairType;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import com.quorum.tessera.nacl.NaclFacadeFactory;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig) {

        if(keyVaultConfig != null) {
            final Config configWithKeyVault = new Config(null, null, null,
                new KeyConfiguration(null, null, null, keyVaultConfig),
                null, null, true, true);

            final EnvironmentVariableProvider envProvider = new EnvironmentVariableProvider();

            if(keyVaultConfig.getVaultType().equals(KeyVaultType.AZURE)) {
                final KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyPairType.AZURE);
                final KeyVaultService keyVaultService = keyVaultServiceFactory.create(configWithKeyVault, envProvider);

                return new AzureVaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);
            }
        }

        return new FileKeyGenerator(
            NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create()
        );
    }
}
