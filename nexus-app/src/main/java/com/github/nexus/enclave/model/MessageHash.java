package com.github.nexus.enclave.model;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.util.Arrays;
import java.util.Base64;

@Embeddable
public class MessageHash {

    @Lob
    private byte[] hashBytes;

    public MessageHash(final byte[] hashBytes) {
        this.hashBytes = Arrays.copyOf(hashBytes, hashBytes.length);
    }

    public MessageHash() {
    }

    public void setHashBytes(final byte[] hashBytes) {
        this.hashBytes = Arrays.copyOf(hashBytes, hashBytes.length);
    }

    public byte[] getHashBytes() {
        return Arrays.copyOf(hashBytes, hashBytes.length);
    }

    @Override
    public boolean equals(final Object o) {
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

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(hashBytes);
    }

}
