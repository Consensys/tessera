package com.quorum.tessera.key.vault.azure;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keypairs.KeyPairType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

public class AzureKeyVaultServiceFactory implements KeyVaultServiceFactory {

    private final String clientIdEnvVar = "AZURE_CLIENT_ID";
    private final String clientSecretEnvVar = "AZURE_CLIENT_SECRET";

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        //TODO null check
        Objects.requireNonNull(config);
        Objects.requireNonNull(envProvider);

        String clientId = envProvider.getEnv(clientIdEnvVar);
        String clientSecret = envProvider.getEnv(clientSecretEnvVar);

        if(clientId == null || clientSecret == null) {
            throw new AzureCredentialNotSetException(clientIdEnvVar + " and " + clientSecretEnvVar + " environment variables must be set");
        }

        //TODO null check
        KeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
            .map(KeyConfiguration::getKeyVaultConfig)
            .orElseThrow(() -> new RuntimeException("Trying to create Azure key vault but no Azure configuration provided in the configfile"));

        return new AzureKeyVaultService(
            keyVaultConfig,
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

    @Override
    public KeyPairType getType() {
        return KeyPairType.AZURE;
    }
}
