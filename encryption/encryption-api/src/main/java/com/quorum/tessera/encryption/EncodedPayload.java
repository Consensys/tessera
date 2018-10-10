package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.Nonce;

import java.util.*;

/**
 * This class contains the base data that is sent to other nodes
 * (it is wrapped further, but the main data that is needed is here)
 */
public class EncodedPayload {

    private final PublicKey senderKey;

    private final byte[] cipherText;

    private final Nonce cipherTextNonce;

    private final List<byte[]> recipientBoxes;

    private final Nonce recipientNonce;

    public EncodedPayload(final PublicKey senderKey,
                          final byte[] cipherText,
                          final Nonce cipherTextNonce,
                          final List<byte[]> recipientBoxes,
                          final Nonce recipientNonce) {

        this.senderKey = senderKey;
        this.cipherText = cipherText;
        this.cipherTextNonce = cipherTextNonce;
        this.recipientNonce = recipientNonce;

        this.recipientBoxes = Collections.unmodifiableList(recipientBoxes);
    }

    public PublicKey getSenderKey() {
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
