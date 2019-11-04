package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.vault.data.AWSGetSecretData;
import com.quorum.tessera.config.vault.data.AWSSetSecretData;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import com.quorum.tessera.nacl.NaclFacadeFactory;
import com.quorum.tessera.passwords.PasswordReaderFactory;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig) {

        if (keyVaultConfig != null) {
            final KeyVaultServiceFactory keyVaultServiceFactory =
                    KeyVaultServiceFactory.getInstance(keyVaultConfig.getKeyVaultType());

            final Config config = new Config();
            final KeyConfiguration keyConfiguration = new KeyConfiguration();

            if (keyVaultConfig.getKeyVaultType().equals(KeyVaultType.AZURE)) {
                keyConfiguration.setAzureKeyVaultConfig((AzureKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                final KeyVaultService<AzureSetSecretData, AzureGetSecretData> keyVaultService =
                        keyVaultServiceFactory.create(config, new EnvironmentVariableProvider());

                return new AzureVaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);

            } else if (keyVaultConfig.getKeyVaultType().equals(KeyVaultType.AWS)) {
                keyConfiguration.setAwsKeyVaultConfig((AWSKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                final KeyVaultService<AWSSetSecretData, AWSGetSecretData> keyVaultService =
                        keyVaultServiceFactory.create(config, new EnvironmentVariableProvider());

                return new AWSSecretManagerKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);
            } else {
                keyConfiguration.setHashicorpKeyVaultConfig((HashicorpKeyVaultConfig) keyVaultConfig);

                config.setKeys(keyConfiguration);

                final KeyVaultService<HashicorpSetSecretData, HashicorpGetSecretData> keyVaultService =
                        keyVaultServiceFactory.create(config, new EnvironmentVariableProvider());

                return new HashicorpVaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);
            }
        }

        return new FileKeyGenerator(
                NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create());
    }
}
