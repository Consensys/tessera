package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.Nonce;

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
}
