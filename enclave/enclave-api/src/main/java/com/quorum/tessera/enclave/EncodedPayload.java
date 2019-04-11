package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;

import java.util.List;

/**
 * This class contains the data that is sent to other nodes
 */
public class EncodedPayload {

    private final PublicKey senderKey;

    private final byte[] cipherText;

    private final Nonce cipherTextNonce;

    private final List<byte[]> recipientBoxes;

    private final Nonce recipientNonce;

    private final List<PublicKey> recipientKeys;

    public EncodedPayload(final PublicKey senderKey,
                          final byte[] cipherText,
                          final Nonce cipherTextNonce,
                          final List<byte[]> recipientBoxes,
                          final Nonce recipientNonce,
                          final List<PublicKey> recipientKeys) {
        this.senderKey = senderKey;
        this.cipherText = cipherText;
        this.cipherTextNonce = cipherTextNonce;
        this.recipientNonce = recipientNonce;
        this.recipientBoxes = recipientBoxes;
        this.recipientKeys = recipientKeys;
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

    public List<PublicKey> getRecipientKeys() {
        return recipientKeys;
    }

}
