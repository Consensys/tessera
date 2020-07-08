package com.quorum.tessera.key.vault.azure;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import java.util.Objects;

class AzureSecretClientDelegate {

    private final SecretClient secretClient;

    AzureSecretClientDelegate(SecretClient secretClient) {
        this.secretClient = Objects.requireNonNull(secretClient);
    }

    public String getVaultUrl() {
        return secretClient.getVaultUrl();
    }

    KeyVaultSecret getSecret(String name, String version) {
        return secretClient.getSecret(name, version);
    }

    KeyVaultSecret setSecret(String name, String value) {
        return secretClient.setSecret(name, value);
    }
}
