package com.github.nexus.nacl;

import java.util.Arrays;


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
        if (this == o) {
            return true;
        }

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
