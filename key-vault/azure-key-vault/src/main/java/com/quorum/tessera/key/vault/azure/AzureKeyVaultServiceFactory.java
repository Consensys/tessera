package com.quorum.tessera.key.vault.azure;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;

import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_SECRET;

public class AzureKeyVaultServiceFactory implements KeyVaultServiceFactory {

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        Objects.requireNonNull(config);
        Objects.requireNonNull(envProvider);

        String clientId = envProvider.getEnv(AZURE_CLIENT_ID);
        String clientSecret = envProvider.getEnv(AZURE_CLIENT_SECRET);

        if (clientId == null || clientSecret == null) {
            throw new AzureCredentialNotSetException(
                    AZURE_CLIENT_ID + " and " + AZURE_CLIENT_SECRET + " environment variables must be set");
        }

        KeyVaultConfig keyVaultConfig =
                Optional.ofNullable(config.getKeys())
                        .map(KeyConfiguration::getKeyVaultConfig)
                        .orElseThrow(
                                () ->
                                        new ConfigException(
                                                new RuntimeException(
                                                        "Trying to create Azure key vault connection but no Azure configuration provided")));

        return new AzureKeyVaultService(
                keyVaultConfig,
                new AzureKeyVaultClientDelegate(
                        new AzureKeyVaultClientFactory(
                                        new AzureKeyVaultClientCredentials(
                                                clientId, clientSecret, Executors.newFixedThreadPool(1)))
                                .getAuthenticatedClient()));
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }
}
