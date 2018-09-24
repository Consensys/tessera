package com.quorum.tessera.key.vault;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;

public class AzureKeyVaultClientDelegate {
    private final KeyVaultClient keyVaultClient;

    public AzureKeyVaultClientDelegate(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = keyVaultClient;
    }

    public SecretBundle getSecret(String vaultBaseUrl, String secretName) {
        return keyVaultClient.getSecret(vaultBaseUrl, secretName);
    }

    public SecretBundle setSecret(SetSecretRequest setSecretRequest) {
        return keyVaultClient.setSecret(setSecretRequest);
    }
}
