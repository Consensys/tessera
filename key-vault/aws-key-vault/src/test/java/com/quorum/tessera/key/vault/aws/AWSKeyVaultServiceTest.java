package com.quorum.tessera.key.vault.aws;

import com.quorum.tessera.config.vault.data.AWSGetSecretData;
import com.quorum.tessera.config.vault.data.AWSSetSecretData;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AWSKeyVaultServiceTest {

    private AWSKeyVaultService keyVaultService;

    private String endpoint = "endpoint";

    private SecretsManagerClient secretsManager;

    @Before
    public void setUp() {
        this.secretsManager = mock(SecretsManagerClient.class);

        this.keyVaultService = new AWSKeyVaultService(secretsManager);
    }

    @Test
    public void getSecret() {
        String secretName = "name";

        AWSGetSecretData getSecretData = mock(AWSGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);

        GetSecretValueResponse secretValueResponse = GetSecretValueResponse.builder().secretString("secret").build();

        when(secretsManager.getSecretValue(Mockito.any(GetSecretValueRequest.class))).thenReturn(secretValueResponse);

        assertThat(keyVaultService.getSecret(getSecretData)).isEqualTo("secret");
    }

    @Test
    public void getSecretThrowsExceptionIfSecretReturnedIsNull() {
        AWSKeyVaultService awsKeyVaultService = new AWSKeyVaultService(secretsManager);

        String secretName = "secret";

        AWSGetSecretData getSecretData = mock(AWSGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);

        Throwable throwable = catchThrowable(() -> awsKeyVaultService.getSecret(getSecretData));

        assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
        assertThat(throwable)
                .hasMessageContaining("The requested secret '" + secretName + "' was not found in AWS Secrets Manager");
    }

    @Test
    public void getSecretThrowsExceptionIfKeyNotFoundInVault() {
        AWSKeyVaultService awsKeyVaultService = new AWSKeyVaultService(secretsManager);

        String secretName = "secret";

        AWSGetSecretData getSecretData = mock(AWSGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);

        when(secretsManager.getSecretValue(Mockito.any(GetSecretValueRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build());

        Throwable throwable = catchThrowable(() -> awsKeyVaultService.getSecret(getSecretData));

        assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
        assertThat(throwable)
                .hasMessageContaining("The requested secret '" + secretName + "' was not found in AWS Secrets Manager");
    }

    @Test
    public void getSecretThrowsExceptionIfAWSException() {
        AWSKeyVaultService awsKeyVaultService = new AWSKeyVaultService(secretsManager);

        String secretName = "secret";

        AWSGetSecretData getSecretData = mock(AWSGetSecretData.class);
        when(getSecretData.getSecretName()).thenReturn(secretName);

        when(secretsManager.getSecretValue(Mockito.any(GetSecretValueRequest.class)))
                .thenThrow(InvalidParameterException.builder().build());

        Throwable throwable = catchThrowable(() -> awsKeyVaultService.getSecret(getSecretData));

        assertThat(throwable).isInstanceOf(AWSSecretsManagerException.class);
    }

    @Test
    public void setSecret() {
        AWSSetSecretData setSecretData = mock(AWSSetSecretData.class);
        String secretName = "id";
        String secret = "secret";
        when(setSecretData.getSecretName()).thenReturn(secretName);
        when(setSecretData.getSecret()).thenReturn(secret);

        keyVaultService.setSecret(setSecretData);

        CreateSecretRequest expected = CreateSecretRequest.builder().name(secretName).secretString(secret).build();

        ArgumentCaptor<CreateSecretRequest> argument = ArgumentCaptor.forClass(CreateSecretRequest.class);
        verify(secretsManager).createSecret(argument.capture());

        assertThat(argument.getValue()).isEqualToComparingFieldByField(expected);
    }
}
