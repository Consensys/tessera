package com.quorum.tessera.key.vault.aws;

import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import java.util.Map;
import java.util.Objects;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

public class AWSKeyVaultService implements KeyVaultService {

  protected static final String SECRET_NAME_KEY = "secretName";

  protected static final String SECRET_KEY = "secret";

  private final SecretsManagerClient secretsManager;

  AWSKeyVaultService(SecretsManagerClient secretsManager) {
    this.secretsManager = Objects.requireNonNull(secretsManager);
  }

  @Override
  public String getSecret(Map<String, String> getSecretData) {

    final String secretName = getSecretData.get(SECRET_NAME_KEY);

    GetSecretValueRequest getSecretValueRequest =
        GetSecretValueRequest.builder().secretId(secretName).build();
    GetSecretValueResponse secretValueResponse;

    try {
      secretValueResponse = secretsManager.getSecretValue(getSecretValueRequest);
    } catch (ResourceNotFoundException e) {
      throw new VaultSecretNotFoundException(
          "The requested secret '" + secretName + "' was not found in AWS Secrets Manager");
    } catch (InvalidRequestException | InvalidParameterException e) {
      throw new AWSSecretsManagerException(e);
    }

    if (secretValueResponse != null && secretValueResponse.secretString() != null) {
      return secretValueResponse.secretString();
    }

    throw new VaultSecretNotFoundException(
        "The requested secret '" + secretName + "' was not found in AWS Secrets Manager");
  }

  @Override
  public Object setSecret(Map<String, String> setSecretData) {

    final String secretName = setSecretData.get(SECRET_NAME_KEY);
    final String secret = setSecretData.get(SECRET_KEY);

    CreateSecretRequest createSecretRequest =
        CreateSecretRequest.builder().name(secretName).secretString(secret).build();

    return secretsManager.createSecret(createSecretRequest);
  }
}
