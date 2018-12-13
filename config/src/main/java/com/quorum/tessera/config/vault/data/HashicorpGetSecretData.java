package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

public class HashicorpGetSecretData implements GetSecretData {
    private final String secretEngineName;
    private final String secretName;
    private final String valueId;

    public HashicorpGetSecretData(String secretEngineName, String secretName, String valueId) {
        this.secretEngineName = secretEngineName;
        this.secretName = secretName;
        this.valueId = valueId;
    }

    public String getSecretEngineName() {
        return secretEngineName;
    }

    public String getSecretName() {
        return secretName;
    }

    public String getValueId() {
        return valueId;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
