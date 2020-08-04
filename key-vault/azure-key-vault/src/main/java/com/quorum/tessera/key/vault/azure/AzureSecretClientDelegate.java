package com.quorum.tessera.key.vault.azure;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

class AzureSecretClientDelegate {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureSecretClientDelegate.class);

    private final SecretClient secretClient;

    AzureSecretClientDelegate(SecretClient secretClient) {
        this.secretClient = Objects.requireNonNull(secretClient);
    }

    public String getVaultUrl() {
        return secretClient.getVaultUrl();
    }

    KeyVaultSecret getSecret(String name, String version) {
        LOGGER.info("CHRISSY getSecret {} {}", name, version);
        return secretClient.getSecret(name, version);
    }

    KeyVaultSecret setSecret(String name, String value) {
        return secretClient.setSecret(name, value);
    }
}
