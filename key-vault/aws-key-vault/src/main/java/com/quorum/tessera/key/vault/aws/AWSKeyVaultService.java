package com.quorum.tessera.key.vault.aws;

import com.quorum.tessera.config.vault.data.AWSGetSecretData;
import com.quorum.tessera.config.vault.data.AWSSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.InvalidRequestException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

public class AWSKeyVaultService implements KeyVaultService<AWSSetSecretData, AWSGetSecretData> {
    private final SecretsManagerClient secretsManager;

    AWSKeyVaultService(SecretsManagerClient secretsManager) {
        this.secretsManager = secretsManager;
    }

    @Override
    public String getSecret(AWSGetSecretData getSecretData) {
        GetSecretValueRequest getSecretValueRequest =
                GetSecretValueRequest.builder()
                        .secretId(getSecretData.getSecretName())
                        .build();
        GetSecretValueResponse secretValueResponse;

        try {
            secretValueResponse = secretsManager.getSecretValue(getSecretValueRequest);
        } catch (ResourceNotFoundException e) {
            throw new VaultSecretNotFoundException(
                    "The requested secret '"
                            + getSecretData.getSecretName()
                            + "' was not found in AWS Secrets Manager");
        } catch (InvalidRequestException | InvalidParameterException e) {
            throw new AWSSecretsManagerException(e);
        }

        if (secretValueResponse != null && secretValueResponse.secretString() != null) {
            return secretValueResponse.secretString();
        }

        throw new VaultSecretNotFoundException(
                "The requested secret '" + getSecretData.getSecretName() + "' was not found in AWS Secrets Manager");
    }

    @Override
    public Object setSecret(AWSSetSecretData setSecretData) {
        CreateSecretRequest createSecretRequest =
                CreateSecretRequest.builder()
                        .name(setSecretData.getSecretName())
                        .secretString(setSecretData.getSecret())
                        .build();

        return secretsManager.createSecret(createSecretRequest);
    }
}
