package com.quorum.tessera.config.vault.data;

import com.quorum.tessera.config.KeyVaultType;

public class AWSSetSecretData implements SetSecretData {
    private final String secretName;
    private final String secret;

    public AWSSetSecretData(String secretName, String secret) {
        this.secretName = secretName;
        this.secret = secret;
    }

    public String getSecretName() {
        return secretName;
    }

    public String getSecret() {
        return secret;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AWS;
    }
}
