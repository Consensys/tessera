package com.quorum.tessera.key.vault;

public interface KeyVaultService {
//    String getSecret(String secretName);
//
//    String getSecretFromPath(String secretPath, String secretName);
//
//    Object setSecret(String secretName, String secret);
//
//    Object setSecretAtPath(String secretPath, Map<String, Object> secretData);

    String getSecret(GetSecretData getSecretData);

    Object setSecret(SetSecretData setSecretData);

}
