package com.quorum.tessera.key.vault.azure;

import com.azure.identity.EnvironmentCredentialBuilder;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import java.util.Objects;
import java.util.Optional;

public class AzureKeyVaultServiceFactory implements KeyVaultServiceFactory {

    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        Objects.requireNonNull(config);

        final KeyVaultConfig keyVaultConfig = Optional.ofNullable(config.getKeys())
            .flatMap(k -> k.getKeyVaultConfig(KeyVaultType.AZURE))
            .orElseThrow(() -> new ConfigException(new RuntimeException("Trying to create Azure key vault connection but no Azure configuration provided")));

        final String url = keyVaultConfig
            .getProperty("url")
            .orElseThrow(() -> new ConfigException(new RuntimeException("No Azure Key Vault url provided")));

        final AzureSecretClientDelegate client = new AzureSecretClientDelegate(
            new AzureSecretClientFactory(url, new EnvironmentCredentialBuilder().build()).create()
        );

        return new AzureKeyVaultService(client);
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }
}
