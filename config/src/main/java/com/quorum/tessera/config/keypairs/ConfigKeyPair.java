package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;

public interface ConfigKeyPair {

    String getPublicKey();

    String getPrivateKey();

    void withPassword(String password);

    String getPassword();

    KeyData marshal();

}
