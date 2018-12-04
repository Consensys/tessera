package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.key.vault.SetSecretData;
import com.quorum.tessera.key.vault.SetSecretDataFactory;

import java.util.Map;

public class HashicorpSetSecretDataFactory implements SetSecretDataFactory {

    @Override
    public SetSecretData create(Map<String, Object> data) {
        if(!data.containsKey("secretPath") || !data.containsKey("nameValuePairs")) {
            throw new IllegalArgumentException("data must contain value with key 'secretPath' and value with key 'nameValuePairs'");
        }

        if(!(data.get("secretPath") instanceof String)) {
            throw new IllegalArgumentException("The value for key 'secretPath' must be of type String for SetSecretData");
        }

        if(!(data.get("nameValuePairs") instanceof Map)) {
            throw new IllegalArgumentException("The value for key 'nameValuePairs' must be of type Map<String, Object> for SetSecretData");
        }

        String secretPath = (String) data.get("secretPath");
        Map<String, Object> nameValuePairs = (Map<String, Object>) data.get("nameValuePairs");

        return new HashicorpSetSecretData(secretPath, nameValuePairs);
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.HASHICORP;
    }
}
