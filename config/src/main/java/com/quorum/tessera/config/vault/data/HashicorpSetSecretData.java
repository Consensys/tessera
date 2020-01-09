package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

import java.util.Map;

public class HashicorpSetSecretData implements SetSecretData {

    private final String secretEngineName;

    private final String secretName;

    private final Map<String, Object> nameValuePairs;

    public HashicorpSetSecretData(String secretEngineName, String secretName, Map<String, Object> nameValuePairs) {
        this.secretEngineName = secretEngineName;
        this.secretName = secretName;
        this.nameValuePairs = nameValuePairs;
    }

    public String getSecretEngineName() {
        return secretEngineName;
    }

    public String getSecretName() {
        return secretName;
    }

    public Map<String, Object> getNameValuePairs() {
        return nameValuePairs;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
