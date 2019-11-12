package com.quorum.tessera.key.vault.aws;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import com.quorum.tessera.config.AWSKeyVaultConfig;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;

public class AWSKeyVaultServiceFactory implements KeyVaultServiceFactory {
    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        AWSKeyVaultConfig keyVaultConfig =
                Optional.ofNullable(config.getKeys())
                        .map(KeyConfiguration::getAwsKeyVaultConfig)
                        .orElseThrow(
                                () ->
                                        new ConfigException(
                                                new RuntimeException(
                                                        "Trying to create AWS Secrets Manager connection but no configuration provided")));

        return new AWSKeyVaultService(getAwsSecretsManager(keyVaultConfig));
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AWS;
    }

    private SecretsManagerClient getAwsSecretsManager(AWSKeyVaultConfig keyVaultConfig) {
        SecretsManagerClientBuilder secretsManagerClient = SecretsManagerClient.builder();

        if (Objects.nonNull(keyVaultConfig.getEndpoint())) {
            secretsManagerClient.endpointOverride(URI.create(keyVaultConfig.getEndpoint()));
        }
        return secretsManagerClient.build();
    }
}
