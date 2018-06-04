package com.github.nexus.enclave.model;

import java.util.Arrays;

public class MessageHash {

    private final byte[] hashBytes;

    public MessageHash(final byte[] hashBytes) {
        this.hashBytes = Arrays.copyOf(hashBytes, hashBytes.length);
    }

    public byte[] getHashBytes() {
        return hashBytes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MessageHash)) {
            return false;
        }

        final MessageHash that = (MessageHash) o;
        return Arrays.equals(getHashBytes(), that.getHashBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getHashBytes());
    }

}
