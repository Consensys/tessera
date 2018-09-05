package com.quorum.tessera.config.keys.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;

public class KeyVaultClientDelegate {
    private final KeyVaultClient keyVaultClient;

    public KeyVaultClientDelegate(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = keyVaultClient;
    }

    public SecretBundle getSecret(String vaultBaseUrl, String secretName) {
        return keyVaultClient.getSecret(vaultBaseUrl, secretName);
    }

}
