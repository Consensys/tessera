package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.KeyData;

public class DirectKeyPair implements ConfigKeyPair {

    private final String publicKey;

    private final String privateKey;

    public DirectKeyPair(final String publicKey, final String privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public KeyData marshal() {
        return new KeyData(null, this.privateKey, this.publicKey, null, null);
    }

    @Override
    public String getPublicKey() {
        return this.publicKey;
    }

    @Override
    public String getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public void withPassword(final String password) {
        //no need to keep a password for this key type
    }

    @Override
    public String getPassword() {
        //no password to return
        return "";
    }

}
