package com.quorum.tessera.key.vault;

import java.util.Map;

public interface KeyVaultService {
    String getSecret(String secretName);

    String getSecretFromPath(String secretPath, String secretName);

    Object setSecret(String secretName, String secret);

    Object setSecretAtPath(String secretPath, Map<String, String> secretData);

}
