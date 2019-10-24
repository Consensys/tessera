package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import java.util.ArrayList;
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

    private EncodedPayload(
            final PublicKey senderKey,
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

    public static class Builder {

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        private PublicKey senderKey;

        private byte[] cipherText;

        private Nonce cipherTextNonce;

        private Nonce recipientNonce;

        private List<byte[]> recipientBoxes = new ArrayList<>();

        private List<PublicKey> recipientKeys = new ArrayList<>();

        public Builder withSenderKey(final PublicKey senderKey) {
            this.senderKey = senderKey;
            return this;
        }

        public Builder withCipherText(final byte[] cipherText) {
            this.cipherText = cipherText;
            return this;
        }

        public Builder withRecipientKeys(final List<PublicKey> recipientKeys) {
            this.recipientKeys = recipientKeys;
            return this;
        }

        public Builder withCipherTextNonce(final Nonce cipherTextNonce) {
            this.cipherTextNonce = cipherTextNonce;
            return this;
        }

        public Builder withCipherTextNonce(final byte[] cipherTextNonce) {
            this.cipherTextNonce = new Nonce(cipherTextNonce);
            return this;
        }

        public Builder withRecipientNonce(final Nonce recipientNonce) {
            this.recipientNonce = recipientNonce;
            return this;
        }

        public Builder withRecipientNonce(final byte[] recipientNonce) {
            this.recipientNonce = new Nonce(recipientNonce);
            return this;
        }

        public Builder withRecipientBoxes(final List<byte[]> recipientBoxes) {
            this.recipientBoxes = recipientBoxes;
            return this;
        }

        public EncodedPayload build() {
            return new EncodedPayload(
                    senderKey, cipherText, cipherTextNonce, recipientBoxes, recipientNonce, recipientKeys);
        }
    }
}
