package com.quorum.tessera.transaction.model;


import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.Nonce;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains the base data that is sent to other nodes
 * (it is wrapped further, but the main data that is needed is here)
 */
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
        this.cipherText = Arrays.copyOf(cipherText, cipherText.length);
        this.cipherTextNonce = cipherTextNonce;
        this.recipientNonce = recipientNonce;

        final List<byte[]> recBoxes = Optional
            .ofNullable(recipientBoxes)
            .orElse(new ArrayList<>())
            .stream()
            .map(arr -> Arrays.copyOf(arr, arr.length))
            .collect(Collectors.toList());

        this.recipientBoxes = Collections.unmodifiableList(recBoxes);
    }

    public Key getSenderKey() {
        return senderKey;
    }

    public byte[] getCipherText() {
        return Arrays.copyOf(this.cipherText, this.cipherText.length);
    }

    public Nonce getCipherTextNonce() {
        return cipherTextNonce;
    }

    public List<byte[]> getRecipientBoxes() {
        return recipientBoxes
            .stream()
            .map(arr -> Arrays.copyOf(arr, arr.length))
            .collect(Collectors.toList());
    }

    public Nonce getRecipientNonce() {
        return recipientNonce;
    }
}
