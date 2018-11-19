package com.quorum.tessera.key.vault.azure;

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;

import java.util.Objects;

public class AzureKeyVaultClientDelegate {
    private final KeyVaultClient keyVaultClient;

    public AzureKeyVaultClientDelegate(KeyVaultClient keyVaultClient) {
        this.keyVaultClient = Objects.requireNonNull(keyVaultClient);
    }

    public SecretBundle getSecret(String vaultBaseUrl, String secretName) {
        return keyVaultClient.getSecret(vaultBaseUrl, secretName);
    }

    public SecretBundle setSecret(SetSecretRequest setSecretRequest) {
        return keyVaultClient.setSecret(setSecretRequest);
    }
}
