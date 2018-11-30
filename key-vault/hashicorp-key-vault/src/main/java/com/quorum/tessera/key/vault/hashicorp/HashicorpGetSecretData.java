package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.key.vault.GetSecretData;

public class HashicorpGetSecretData implements GetSecretData {
    private String secretPath;

    private String secretName;

    public HashicorpGetSecretData(String secretPath, String secretName) {
        this.secretPath = secretPath;
        this.secretName = secretName;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public String getSecretName() {
        return secretName;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
