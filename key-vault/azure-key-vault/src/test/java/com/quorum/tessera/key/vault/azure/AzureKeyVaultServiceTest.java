package com.quorum.tessera.key.vault.azure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AzureKeyVaultServiceTest {

  private AzureKeyVaultService keyVaultService;

  private SecretClient secretClient;

  @Before
  public void beforeTest() {
    this.secretClient = mock(SecretClient.class);
    this.keyVaultService = new AzureKeyVaultService(secretClient);
  }

  @Test
  public void afterTest() {
    verifyNoMoreInteractions(secretClient);
  }

  @Test
  public void getSecret() {
    final String secretName = "secret-name";
    final String secretVersion = "secret-version";

    final Map<String, String> getSecretData =
        Map.of(
            AzureKeyVaultService.SECRET_NAME_KEY, secretName,
            AzureKeyVaultService.SECRET_VERSION_KEY, secretVersion);

    final String expectedSecretValue = "secret-value";
    final KeyVaultSecret gotSecret = mock(KeyVaultSecret.class);
    when(gotSecret.getValue()).thenReturn(expectedSecretValue);

    when(secretClient.getSecret(anyString(), anyString())).thenReturn(gotSecret);

    final String result = keyVaultService.getSecret(getSecretData);

    assertThat(result).isEqualTo(expectedSecretValue);
    verify(secretClient).getSecret(secretName, secretVersion);
  }

  @Test
  public void getSecretThrowsExceptionIfKeyNotFoundInVault() {
    final String secretName = "secret-name";
    final String secretVersion = "secret-version";

    final Map<String, String> getSecretData =
        Map.of(
            AzureKeyVaultService.SECRET_NAME_KEY, secretName,
            AzureKeyVaultService.SECRET_VERSION_KEY, secretVersion);

    final ResourceNotFoundException toThrow =
        new ResourceNotFoundException("oh no", mock(HttpResponse.class));
    when(secretClient.getSecret(anyString(), anyString())).thenThrow(toThrow);

    when(secretClient.getVaultUrl()).thenReturn("vault-url");

    final Throwable ex = catchThrowable(() -> keyVaultService.getSecret(getSecretData));

    assertThat(ex).isExactlyInstanceOf(VaultSecretNotFoundException.class);
    assertThat(ex)
        .hasMessage("Azure Key Vault secret secret-name was not found in vault vault-url");
    verify(secretClient).getSecret("secret-name", "secret-version");
  }

  @Test
  public void setSecret() {

    final String secretName = "secret-name";
    final String secret = "secret-value";

    final Map<String, String> setSecretData =
        Map.of(
            AzureKeyVaultService.SECRET_NAME_KEY, secretName,
            AzureKeyVaultService.SECRET_KEY, secret);

    final KeyVaultSecret newSecret = mock(KeyVaultSecret.class);
    when(secretClient.setSecret(secretName, secret)).thenReturn(newSecret);

    final Object result = keyVaultService.setSecret(setSecretData);

    assertThat(result).isSameAs(newSecret);
    verify(secretClient).setSecret("secret-name", "secret-value");
  }
}
