package com.quorum.tessera.key.vault;

public interface KeyVaultService {
    String getSecret(String secretName);

    Object setSecret(String secretName, String secret);

}
