package com.quorum.tessera.key.vault.azure;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
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

    final KeyVaultConfig keyVaultConfig =
        Optional.ofNullable(config.getKeys())
            .flatMap(k -> k.getKeyVaultConfig(KeyVaultType.AZURE))
            .orElseThrow(
                () ->
                    new ConfigException(
                        new RuntimeException(
                            "Trying to create Azure key vault connection but no Azure configuration provided")));

    final String url =
        keyVaultConfig
            .getProperty("url")
            .orElseThrow(
                () -> new ConfigException(new RuntimeException("No Azure Key Vault url provided")));

    final SecretClient secretClient =
        new SecretClientBuilder()
            .vaultUrl(url)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    return new AzureKeyVaultService(secretClient);
  }

  @Override
  public KeyVaultType getType() {
    return KeyVaultType.AZURE;
  }
}
