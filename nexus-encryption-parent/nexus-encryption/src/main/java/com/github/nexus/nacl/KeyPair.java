package com.github.nexus.nacl;

import java.util.Objects;

public class KeyPair {

    private final Key publicKey;

    private final Key privateKey;

    public KeyPair(final Key publicKey, final Key privateKey) {
        this.publicKey = Objects.requireNonNull(publicKey);
        this.privateKey = Objects.requireNonNull(privateKey);
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public Key getPrivateKey() {
        return privateKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof KeyPair) &&
                Objects.equals(publicKey, ((KeyPair) o).publicKey) &&
                Objects.equals(privateKey, ((KeyPair) o).privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPublicKey(), getPrivateKey());
    }
}
