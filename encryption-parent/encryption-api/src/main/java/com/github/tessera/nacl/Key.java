package com.github.tessera.nacl;

import java.util.Arrays;
import java.util.Base64;

/**
 * Represents a Key, which is usually 32 bytes in length
 * The possible types of keys include:
 * - public
 * - private
 * - symmetric
 */
public class Key {

    private final byte[] key;

    public Key(final byte[] keyBytes) {
        this.key = Arrays.copyOf(keyBytes, keyBytes.length);
    }

    public byte[] getKeyBytes() {
        return Arrays.copyOf(this.key, this.key.length);
    }

    @Override
    public boolean equals(final Object o) {
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
