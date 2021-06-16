package com.quorum.tessera.key.vault.aws;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import java.util.Map;
import org.junit.After;
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

public class AWSKeyVaultServiceTest {

  private AWSKeyVaultService keyVaultService;

  private SecretsManagerClient secretsManager;

  @Before
  public void beforeTest() {
    this.secretsManager = mock(SecretsManagerClient.class);
    this.keyVaultService = new AWSKeyVaultService(secretsManager);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(secretsManager);
  }

  @Test
  public void getSecret() {
    String secretName = "name";

    Map<String, String> getSecretData = Map.of(AWSKeyVaultService.SECRET_NAME_KEY, secretName);

    GetSecretValueResponse secretValueResponse =
        GetSecretValueResponse.builder().secretString("secret").build();

    when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
        .thenReturn(secretValueResponse);

    assertThat(keyVaultService.getSecret(getSecretData)).isEqualTo("secret");

    verify(secretsManager).getSecretValue(any(GetSecretValueRequest.class));
  }

  @Test
  public void getSecretThrowsExceptionIfSecretReturnedIsNull() {
    String secretName = "secret";

    Map<String, String> getSecretData = Map.of(AWSKeyVaultService.SECRET_NAME_KEY, secretName);

    Throwable throwable = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
    assertThat(throwable)
        .hasMessageContaining(
            "The requested secret '" + secretName + "' was not found in AWS Secrets Manager");

    verify(secretsManager).getSecretValue(any(GetSecretValueRequest.class));
  }

  @Test
  public void getSecretThrowsExceptionIfKeyNotFoundInVault() {
    String secretName = "secret";

    Map<String, String> getSecretData = Map.of(AWSKeyVaultService.SECRET_NAME_KEY, secretName);

    when(secretsManager.getSecretValue(Mockito.any(GetSecretValueRequest.class)))
        .thenThrow(ResourceNotFoundException.builder().build());

    Throwable throwable = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
    assertThat(throwable)
        .hasMessageContaining(
            "The requested secret '" + secretName + "' was not found in AWS Secrets Manager");

    verify(secretsManager).getSecretValue(any(GetSecretValueRequest.class));
  }

  @Test
  public void getSecretThrowsExceptionIfAWSException() {
    String secretName = "secret";

    Map<String, String> getSecretData = Map.of(AWSKeyVaultService.SECRET_NAME_KEY, secretName);

    when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
        .thenThrow(InvalidParameterException.builder().build());

    Throwable throwable = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(throwable).isInstanceOf(AWSSecretsManagerException.class);

    verify(secretsManager).getSecretValue(any(GetSecretValueRequest.class));
  }

  @Test
  public void setSecret() {

    String secretName = "id";
    String secret = "secret";

    Map<String, String> setSecretData =
        Map.of(
            AWSKeyVaultService.SECRET_NAME_KEY, secretName,
            AWSKeyVaultService.SECRET_KEY, secret);

    keyVaultService.setSecret(setSecretData);

    CreateSecretRequest expected =
        CreateSecretRequest.builder().name(secretName).secretString(secret).build();

    ArgumentCaptor<CreateSecretRequest> argument =
        ArgumentCaptor.forClass(CreateSecretRequest.class);
    verify(secretsManager).createSecret(argument.capture());

    assertThat(argument.getValue()).isEqualToComparingFieldByField(expected);
  }

  @Test
  public void getSecretSecretManagerRerurnsNull() {
    String secretName = "name";

    Map<String, String> getSecretData = Map.of(AWSKeyVaultService.SECRET_NAME_KEY, secretName);

    GetSecretValueResponse secretValueResponse = null;

    when(secretsManager.getSecretValue(any(GetSecretValueRequest.class)))
        .thenReturn(secretValueResponse);

    try {
      keyVaultService.getSecret(getSecretData);
      failBecauseExceptionWasNotThrown(VaultSecretNotFoundException.class);
    } catch (VaultSecretNotFoundException vaultSecretNotFoundException) {
      verify(secretsManager).getSecretValue(any(GetSecretValueRequest.class));
      assertThat(vaultSecretNotFoundException)
          .hasMessage(
              "The requested secret '" + secretName + "' was not found in AWS Secrets Manager");
    }
  }
}
