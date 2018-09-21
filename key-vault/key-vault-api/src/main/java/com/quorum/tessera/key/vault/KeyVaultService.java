package com.quorum.tessera.key.vault;

public interface KeyVaultService {
    String getSecret(String secretName);

    void saveSecret(String secretName, String secret);

}
