package com.github.nexus.enclave.keys.model;

import java.util.Arrays;
import java.util.Base64;

public class Key {

    private final byte[] key;

    public Key(final byte[] keyBytes) {
        this.key = Arrays.copyOf(keyBytes, keyBytes.length);
    }

    public byte[] getKeyBytes() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return (o instanceof Key) && Arrays.equals(key, ((Key) o).key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(this.key);
    }
}
