package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.Nonce;
import java.util.Arrays;

import java.util.Objects;

public class RawTransaction {
    private final byte[] encryptedPayload;
    private final byte[] encryptedKey;
    private final Nonce nonce;
    private final PublicKey from;

    public RawTransaction(byte[] encryptedPayload, byte[] encryptedKey, Nonce nonce, PublicKey from) {
        this.encryptedPayload = Objects.requireNonNull(encryptedPayload);
        this.encryptedKey = Objects.requireNonNull(encryptedKey);
        this.nonce = Objects.requireNonNull(nonce);
        this.from = Objects.requireNonNull(from);
    }

    public byte[] getEncryptedPayload() {
        return encryptedPayload;
    }

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public Nonce getNonce() {
        return nonce;
    }

    public PublicKey getFrom() {
        return from;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Arrays.hashCode(this.encryptedPayload);
        hash = 61 * hash + Arrays.hashCode(this.encryptedKey);
        hash = 61 * hash + Objects.hashCode(this.nonce);
        hash = 61 * hash + Objects.hashCode(this.from);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RawTransaction other = (RawTransaction) obj;
        if (!Arrays.equals(this.encryptedPayload, other.encryptedPayload)) {
            return false;
        }
        if (!Arrays.equals(this.encryptedKey, other.encryptedKey)) {
            return false;
        }
        if (!Objects.equals(this.nonce, other.nonce)) {
            return false;
        }
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        return true;
    }

}
