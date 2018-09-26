package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.quorum.tessera.config.KeyVaultConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

        KeyVaultConfig keyVaultConfig = new KeyVaultConfig(vaultUrl);

        when(azureKeyVaultClientDelegate.getSecret(anyString(), anyString())).thenReturn(null);

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        Throwable throwable = catchThrowable(() -> azureKeyVaultService.getSecret(secretName));

        assertThat(throwable).isInstanceOf(RuntimeException.class);
        assertThat(throwable).hasMessageContaining("Azure Key Vault secret " + secretName + " was not found in vault " + vaultUrl);
    }

    @Test
    public void getSecretUsingUrlInConfig() {
        String url = "url";
        String secretId = "id";

        KeyVaultConfig keyVaultConfig = new KeyVaultConfig(url);

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
        KeyVaultConfig keyVaultConfig = new KeyVaultConfig("url");

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);

        String secretName = "id";
        String secret = "secret";

        azureKeyVaultService.setSecret(secretName, secret);

        SetSecretRequest expected = new SetSecretRequest.Builder(keyVaultConfig.getUrl(), secretName, secret).build();

        ArgumentCaptor<SetSecretRequest> argument = ArgumentCaptor.forClass(SetSecretRequest.class);
        verify(azureKeyVaultClientDelegate).setSecret(argument.capture());

        assertThat(argument.getValue()).isEqualToComparingFieldByField(expected);
    }
}
