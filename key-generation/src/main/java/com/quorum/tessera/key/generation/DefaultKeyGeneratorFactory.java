package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.PasswordReaderFactory;
import com.quorum.tessera.key.vault.*;
import com.quorum.tessera.nacl.NaclFacadeFactory;

import java.util.concurrent.Executors;

public class DefaultKeyGeneratorFactory implements KeyGeneratorFactory {

    @Override
    public KeyGenerator create(KeyVaultConfig keyVaultConfig, EnvironmentVariableProvider envProvider) {
        if(keyVaultConfig != null) {
            final KeyVaultService keyVaultService = createAzureKeyVaultService(keyVaultConfig, envProvider);

            return new AzureVaultKeyGenerator(NaclFacadeFactory.newFactory().create(), keyVaultService);
        }

        return new FileKeyGenerator(
            NaclFacadeFactory.newFactory().create(), KeyEncryptorFactory.create(), PasswordReaderFactory.create()
        );
    }

    private KeyVaultService createAzureKeyVaultService(KeyVaultConfig keyVaultConfig, EnvironmentVariableProvider envProvider) {
        String clientId = envProvider.getEnv("AZURE_CLIENT_ID");
        String clientSecret = envProvider.getEnv("AZURE_CLIENT_SECRET");

        if(clientId == null || clientSecret == null) {
            throw new AzureCredentialNotSetException("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
        }

        return new AzureKeyVaultService(
            new KeyVaultConfig(keyVaultConfig.getUrl()),
            new AzureKeyVaultClientDelegate(
                new AzureKeyVaultClientFactory(
                    new AzureKeyVaultClientCredentials(
                        clientId,
                        clientSecret,
                        Executors.newFixedThreadPool(1)
                    )
                ).getAuthenticatedClient()
            )
        );
    }
}
