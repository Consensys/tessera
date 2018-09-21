package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.quorum.tessera.config.KeyConfiguration;

import java.util.Objects;

public class AzureKeyVaultService implements KeyVaultService {
    private String vaultUrl;
    private AzureKeyVaultClientDelegate azureKeyVaultClientDelegate;

    public AzureKeyVaultService(KeyConfiguration keyConfig, AzureKeyVaultClientDelegate azureKeyVaultClientDelegate) {
        if(Objects.nonNull(keyConfig.getAzureKeyVaultConfig())) {
            this.vaultUrl = keyConfig.getAzureKeyVaultConfig().getUrl();
        }

        this.azureKeyVaultClientDelegate = azureKeyVaultClientDelegate;
    }

    public String getSecret(String secretName) {
        SecretBundle secretBundle = azureKeyVaultClientDelegate.getSecret(vaultUrl, secretName);

        return secretBundle.value();
    }

    @Override
    public void saveSecret(String secretName, String secret) {

    }
}
