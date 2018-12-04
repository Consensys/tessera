package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

import java.util.Map;

public class HashicorpSetSecretData implements SetSecretData {

    private String secretPath;

    private Map<String, Object> nameValuePairs;

    public HashicorpSetSecretData(String secretPath, Map<String, Object> nameValuePairs) {
        this.secretPath = secretPath;
        this.nameValuePairs = nameValuePairs;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public Map<String, Object> getNameValuePairs() {
        return nameValuePairs;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
