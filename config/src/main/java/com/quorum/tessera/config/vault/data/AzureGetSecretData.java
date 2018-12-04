package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

public class AzureGetSecretData implements GetSecretData {

    private String secretName;

    public AzureGetSecretData(String secretName) {
        this.secretName = secretName;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }

    public String getSecretName() {
        return secretName;
    }
}
