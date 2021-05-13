package com.quorum.tessera.key.vault.aws;

import static com.quorum.tessera.config.util.EnvironmentVariables.AWS_ACCESS_KEY_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AWS_SECRET_ACCESS_KEY;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;

public class AWSKeyVaultServiceFactory implements KeyVaultServiceFactory {
  @Override
  public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
    String accessKeyID = envProvider.getEnv(AWS_ACCESS_KEY_ID);
    String secretAccessKey = envProvider.getEnv(AWS_SECRET_ACCESS_KEY);

    if ((accessKeyID != null && secretAccessKey == null)
        || (secretAccessKey != null && accessKeyID == null)) {
      throw new IncompleteAWSCredentialsException(
          "If using environment variables, both "
              + AWS_ACCESS_KEY_ID
              + " and "
              + AWS_SECRET_ACCESS_KEY
              + " must be set");
    }

    KeyVaultConfig keyVaultConfig =
        Optional.ofNullable(config.getKeys())
            .flatMap(k -> k.getKeyVaultConfig(KeyVaultType.AWS))
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

  private SecretsManagerClient getAwsSecretsManager(KeyVaultConfig keyVaultConfig) {
    SecretsManagerClientBuilder secretsManagerClient = SecretsManagerClient.builder();

    Optional<String> endpoint = keyVaultConfig.getProperty("endpoint");
    endpoint.ifPresent(
        s -> {
          final URI uri;

          try {
            uri = new URI(s);
          } catch (URISyntaxException e) {
            throw new ConfigException(new RuntimeException("Invalid AWS endpoint URL provided"));
          }

          if (Objects.isNull(uri.getScheme())) {
            throw new ConfigException(
                new RuntimeException("Invalid AWS endpoint URL provided - no scheme"));
          }

          secretsManagerClient.endpointOverride(uri);
        });
    return secretsManagerClient.build();
  }
}
