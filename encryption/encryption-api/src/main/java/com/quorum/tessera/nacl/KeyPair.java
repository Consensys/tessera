package com.quorum.tessera.nacl;

import java.util.Objects;

/**
 * Container object for a public/private key pair
 *
 * The public and private key should be related to each other, not just an
 * arbitrary pairing of two keys
 */
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
        return (o instanceof KeyPair) &&
            Objects.equals(publicKey, ((KeyPair) o).publicKey) &&
            Objects.equals(privateKey, ((KeyPair) o).privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPublicKey(), getPrivateKey());
    }

}
