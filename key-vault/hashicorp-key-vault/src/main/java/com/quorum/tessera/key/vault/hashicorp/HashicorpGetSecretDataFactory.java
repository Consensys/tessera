package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.key.vault.GetSecretData;
import com.quorum.tessera.key.vault.GetSecretDataFactory;

import java.util.Map;

public class HashicorpGetSecretDataFactory implements GetSecretDataFactory {

    @Override
    public GetSecretData create(Map<String, String> data) {
        if(!data.containsKey("secretName") || !data.containsKey("secretPath")) {
            throw new IllegalArgumentException("data must contain value with key 'secretName' and value with key 'secretPath'");
        }

        return new HashicorpGetSecretData(data.get("secretPath"), data.get("secretName"));
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
