package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.models.SecretBundle;
import com.quorum.tessera.config.KeyConfiguration;

import java.util.Objects;

public class KeyVaultService {
    private String vaultUrl;
    private KeyVaultClientDelegate keyVaultClientDelegate;

    public KeyVaultService(KeyConfiguration keyConfig, KeyVaultClientDelegate keyVaultClientDelegate) {
        if(Objects.nonNull(keyConfig.getKeyVaultConfig())) {
            this.vaultUrl = keyConfig.getKeyVaultConfig().getUrl();
        }

        this.keyVaultClientDelegate = keyVaultClientDelegate;
    }

    public String getSecret(String secretName) {
        SecretBundle secretBundle = keyVaultClientDelegate.getSecret(vaultUrl, secretName);

        return secretBundle.value();
    }
}
