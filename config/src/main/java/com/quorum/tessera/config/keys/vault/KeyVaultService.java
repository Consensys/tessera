package com.quorum.tessera.config.keys.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;

public class KeyVaultService {
    String vaultUrl;

    public KeyVaultService(String vaultUrl) {
        this.vaultUrl = vaultUrl;
    }

    public String getSecretValue(String secretName) {
        KeyVaultClient vaultClient = KeyVaultAuthenticator.getAuthenticatedClient();

        SecretBundle secretBundle = vaultClient.getSecret(vaultUrl, secretName);

        return secretBundle.value();
    }
}
