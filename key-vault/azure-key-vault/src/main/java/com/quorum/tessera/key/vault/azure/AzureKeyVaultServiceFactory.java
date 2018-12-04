package com.quorum.tessera.key.vault.azure;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

public class AzureKeyVaultServiceFactory implements KeyVaultServiceFactory {

    private final String clientIdEnvVar = "AZURE_CLIENT_ID";
    private final String clientSecretEnvVar = "AZURE_CLIENT_SECRET";

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider, KeyVaultClientFactory keyVaultClientFactory) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(envProvider);

        String clientId = envProvider.getEnv(clientIdEnvVar);
        String clientSecret = envProvider.getEnv(clientSecretEnvVar);

        if(clientId == null || clientSecret == null) {
            throw new AzureCredentialNotSetException(clientIdEnvVar + " and " + clientSecretEnvVar + " environment variables must be set");
        }

        AzureKeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
            .map(KeyConfiguration::getAzureKeyVaultConfig)
            .orElseThrow(() -> new ConfigException(new RuntimeException("Trying to create Azure key vault connection but no Azure configuration provided")));

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
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }
}
