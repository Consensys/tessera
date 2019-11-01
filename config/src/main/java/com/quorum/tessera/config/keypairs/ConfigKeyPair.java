package com.quorum.tessera.config.keypairs;

public interface ConfigKeyPair {

    String getPublicKey();

    String getPrivateKey();

    void withPassword(String password);

    String getPassword();
}
