package com.quorum.tessera.key.vault.azure;

import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.key.vault.GetSecretData;

public class AzureGetSecretData implements GetSecretData {

    private String secretName;

    public AzureGetSecretData(String secretName) {
        this.secretName = secretName;
    }

    public String getSecretName() {
        return secretName;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }
}
