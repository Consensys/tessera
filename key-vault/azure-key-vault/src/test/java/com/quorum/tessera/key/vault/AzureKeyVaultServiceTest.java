package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AzureKeyVaultServiceTest {
    @Test
    public void getSecretUsingUrlInConfig() {
        String url = "url";
        String secretId = "id";

        KeyVaultConfig keyVaultConfig = new KeyVaultConfig(url);

        AzureKeyVaultClientDelegate azureKeyVaultClientDelegate = mock(AzureKeyVaultClientDelegate.class);
        when(azureKeyVaultClientDelegate.getSecret(url, secretId)).thenReturn(new SecretBundle());

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(keyVaultConfig, azureKeyVaultClientDelegate);
        azureKeyVaultService.getSecret(secretId);

        verify(azureKeyVaultClientDelegate).getSecret(url, secretId);
    }

    @Test
    public void vaultUrlIsNotSetIfKeyVaultConfigNotDefined() {
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, null, null);
        AzureKeyVaultClientDelegate azureKeyVaultClientDelegate = mock(AzureKeyVaultClientDelegate.class);
        when(azureKeyVaultClientDelegate.getSecret(any(), any())).thenReturn(new SecretBundle());

        AzureKeyVaultService azureKeyVaultService = new AzureKeyVaultService(null, azureKeyVaultClientDelegate);

        azureKeyVaultService.getSecret("secret");

        verify(azureKeyVaultClientDelegate).getSecret(null, "secret");
    }
}
