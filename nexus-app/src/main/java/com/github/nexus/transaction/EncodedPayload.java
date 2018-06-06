package com.github.nexus.transaction;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.encryption.Nonce;

import java.util.*;

public class EncodedPayload {

    private final Key senderKey;

    private final byte[] cipherText;

    private final Nonce cipherTextNonce;

    private final List<byte[]> recipientBoxes;

    private final Nonce recipientNonce;

    public EncodedPayload(final Key senderKey,
                          final byte[] cipherText,
                          final Nonce cipherTextNonce,
                          final List<byte[]> recipientBoxes,
                          final Nonce recipientNonce) {

        this.senderKey = senderKey;
        this.cipherText = cipherText;
        this.cipherTextNonce = cipherTextNonce;
        this.recipientNonce = recipientNonce;

        final List<byte[]> recBoxes = Optional
            .ofNullable(recipientBoxes)
            .orElse(new ArrayList<>());

        this.recipientBoxes = Collections.unmodifiableList(recBoxes);
    }

    public Key getSenderKey() {
        return senderKey;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public Nonce getCipherTextNonce() {
        return cipherTextNonce;
    }

    public List<byte[]> getRecipientBoxes() {
        return recipientBoxes;
    }

    public Nonce getRecipientNonce() {
        return recipientNonce;
    }
}
