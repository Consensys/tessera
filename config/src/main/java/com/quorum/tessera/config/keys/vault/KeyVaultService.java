package com.quorum.tessera.config.keys.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;

public class KeyVaultService {
    private String vaultUrl;

    public KeyVaultService(String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }

    public String getSecret(String secretName) {
        KeyVaultClient vaultClient = KeyVaultAuthenticator.getAuthenticatedClient();

        SecretBundle secretBundle = vaultClient.getSecret(vaultUrl, secretName);

        return secretBundle.value();
    }

    public static KeyVaultService create(String url) {
        return new KeyVaultService(url);
    }
}
