package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyVaultConfig;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class KeyVaultServiceTest {
    @Test
    public void getSecretUsingUrlInConfig() {
        String url = "url";
        String secretId = "id";

        KeyConfiguration keyConfig = new KeyConfiguration(
            null,
            null,
            null,
            new KeyVaultConfig(
                url
            )
        );

        KeyVaultClientDelegate keyVaultClientDelegate = mock(KeyVaultClientDelegate.class);
        when(keyVaultClientDelegate.getSecret(url, secretId)).thenReturn(new SecretBundle());

        KeyVaultService keyVaultService = new KeyVaultService(keyConfig, keyVaultClientDelegate);
        keyVaultService.getSecret(secretId);

        verify(keyVaultClientDelegate).getSecret(url, secretId);
    }

    @Test
    public void vaultUrlIsNotSetIfKeyVaultConfigNotDefined() {
        KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, null, null);
        KeyVaultClientDelegate keyVaultClientDelegate = mock(KeyVaultClientDelegate.class);
        when(keyVaultClientDelegate.getSecret(any(), any())).thenReturn(new SecretBundle());

        KeyVaultService keyVaultService = new KeyVaultService(keyConfiguration, keyVaultClientDelegate);

        keyVaultService.getSecret("secret");

        verify(keyVaultClientDelegate).getSecret(null, "secret");
    }
}
