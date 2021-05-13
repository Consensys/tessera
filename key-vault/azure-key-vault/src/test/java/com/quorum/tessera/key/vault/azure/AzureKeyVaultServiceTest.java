package com.quorum.tessera.key.vault.azure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.config.vault.data.AzureSetSecretData;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import org.junit.Before;
import org.junit.Test;

public class AzureKeyVaultServiceTest {

  private AzureKeyVaultService keyVaultService;

  private AzureSecretClientDelegate secretClient;

  @Before
  public void setUp() {
    this.secretClient = mock(AzureSecretClientDelegate.class);
    this.keyVaultService = new AzureKeyVaultService(secretClient);
  }

  @Test
  public void getSecret() {
    final AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
    when(getSecretData.getSecretName()).thenReturn("secret-name");
    when(getSecretData.getSecretVersion()).thenReturn("secret-version");

    final KeyVaultSecret gotSecret = mock(KeyVaultSecret.class);
    when(gotSecret.getValue()).thenReturn("secret-value");

    when(secretClient.getSecret(anyString(), anyString())).thenReturn(gotSecret);

    final String result = keyVaultService.getSecret(getSecretData);

    assertThat(result).isEqualTo("secret-value");
    verify(secretClient).getSecret("secret-name", "secret-version");
  }

  @Test
  public void getSecretThrowsExceptionIfKeyNotFoundInVault() {
    final AzureGetSecretData getSecretData = mock(AzureGetSecretData.class);
    when(getSecretData.getSecretName()).thenReturn("secret-name");
    when(getSecretData.getSecretVersion()).thenReturn("secret-version");

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
    final AzureSetSecretData setSecretData = mock(AzureSetSecretData.class);
    when(setSecretData.getSecretName()).thenReturn("secret-name");
    when(setSecretData.getSecret()).thenReturn("secret-value");

    final KeyVaultSecret newSecret = mock(KeyVaultSecret.class);
    when(secretClient.setSecret("secret-name", "secret-value")).thenReturn(newSecret);

    final Object result = keyVaultService.setSecret(setSecretData);

    assertThat(result).isInstanceOf(KeyVaultSecret.class);
    assertThat(result).isEqualTo(newSecret);
    verify(secretClient).setSecret("secret-name", "secret-value");
  }
}
