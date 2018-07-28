package com.quorum.tessera.nacl;

import java.util.Arrays;

/**
 * A set of random bytes intended for one time use to encrypt a message
 */
public class Nonce {

    private final byte[] nonceBytes;

    public Nonce(final byte[] nonceBytes) {
        this.nonceBytes = Arrays.copyOf(nonceBytes, nonceBytes.length);
    }

    public byte[] getNonceBytes() {
        return Arrays.copyOf(nonceBytes, nonceBytes.length);
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof Nonce) && Arrays.equals(nonceBytes, ((Nonce) o).nonceBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getNonceBytes());
    }

    @Override
    public String toString() {
        return Arrays.toString(nonceBytes);
    }

}
