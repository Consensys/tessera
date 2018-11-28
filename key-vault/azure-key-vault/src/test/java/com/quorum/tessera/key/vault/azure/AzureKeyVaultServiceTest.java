package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.key.vault.VaultSecretNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class AzureKeyVaultServiceTest {
    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    @Before
    public void setUp() {
        this.azureKeyVaultClientDelegate = mock(AzureKeyVaultClientDelegate.class);
    }

    @Test
    public void exceptionThrownIfKeyNotFoundInVault() {
        String secretName = "secret";
        String vaultUrl = "vaultUrl";

        AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig(vaultUrl);

        when(azureKeyVaultClientDelegate.getSecret(anyString(), anyString())).thenReturn(null);

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        Throwable throwable = catchThrowable(() -> azureKeyVaultService.getSecret(secretName));

        assertThat(throwable).isInstanceOf(VaultSecretNotFoundException.class);
        assertThat(throwable).hasMessageContaining("Azure Key Vault secret " + secretName + " was not found in vault " + vaultUrl);
    }

    @Test
    public void getSecretUsingUrlInConfig() {
        String url = "url";
        String secretId = "id";

        AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig(url);

        when(azureKeyVaultClientDelegate.getSecret(url, secretId)).thenReturn(new SecretBundle());

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);
        azureKeyVaultService.getSecret(secretId);

        verify(azureKeyVaultClientDelegate).getSecret(url, secretId);
    }

    @Test
    public void vaultUrlIsNotSetIfKeyVaultConfigNotDefined() {
        when(azureKeyVaultClientDelegate.getSecret(any(), any())).thenReturn(new SecretBundle());

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(null, azureKeyVaultClientDelegate);

        azureKeyVaultService.getSecret("secret");

        verify(azureKeyVaultClientDelegate).getSecret(null, "secret");
    }

    @Test
    public void setSecretRequestIsUsedToRetrieveSecretFromVault() {
        AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig("url");

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        String secretName = "id";
        String secret = "secret";

        azureKeyVaultService.setSecret(secretName, secret);

        SetSecretRequest expected = new SetSecretRequest.Builder(keyVaultConfig.getUrl(), secretName, secret).build();

        ArgumentCaptor<SetSecretRequest> argument = ArgumentCaptor.forClass(SetSecretRequest.class);
        verify(azureKeyVaultClientDelegate).setSecret(argument.capture());

        assertThat(argument.getValue()).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void getSecretFromPathDoesNotInteractWithAzureClient() {
        AzureKeyVaultConfig keyVaultConfig = mock(AzureKeyVaultConfig.class);

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        assertThat(azureKeyVaultService.getSecretFromPath("secretPath", "secretName")).isNull();
        verifyZeroInteractions(azureKeyVaultClientDelegate);
    }

    @Test
    public void setSecretAtPathReturnsNull() {
        AzureKeyVaultConfig keyVaultConfig = mock(AzureKeyVaultConfig.class);

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        assertThat(azureKeyVaultService.setSecretAtPath("secretPath", Collections.singletonMap("secretName", "secretValue"))).isNull();
        verifyZeroInteractions(azureKeyVaultClientDelegate);
    }
}
