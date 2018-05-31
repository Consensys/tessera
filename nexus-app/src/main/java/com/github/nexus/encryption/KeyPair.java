package com.github.nexus.encryption;

import java.util.Arrays;

public class KeyPair {

    private final byte[] publicKey;

    private final byte[] privateKey;

    public KeyPair(final byte[] publicKey, final byte[] privateKey) {
        this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
        this.privateKey = Arrays.copyOf(privateKey, privateKey.length);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

}
