package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;

import java.util.*;
import java.util.stream.Collectors;

/** This class contains the data that is sent to other nodes */
public class EncodedPayload {

    private final PublicKey senderKey;

    private final byte[] cipherText;

    private final Nonce cipherTextNonce;

    private final List<RecipientBox> recipientBoxes;

    private final Nonce recipientNonce;

    private final List<PublicKey> recipientKeys;

    private EncodedPayload(
            final PublicKey senderKey,
            final byte[] cipherText,
            final Nonce cipherTextNonce,
            final List<RecipientBox> recipientBoxes,
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

    public List<RecipientBox> getRecipientBoxes() {
        return Collections.unmodifiableList(recipientBoxes);
    }

    public Nonce getRecipientNonce() {
        return recipientNonce;
    }

    public List<PublicKey> getRecipientKeys() {
        return Collections.unmodifiableList(recipientKeys);
    }

    public static class Builder {

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        public static Builder from(EncodedPayload encodedPayload) {

            return create().withSenderKey(encodedPayload.getSenderKey())
                    .withRecipientNonce(encodedPayload.getRecipientNonce())
                    .withRecipientKeys(encodedPayload.getRecipientKeys())
                    .withRecipientBoxes(
                            encodedPayload.getRecipientBoxes().stream()
                                    .map(RecipientBox::getData)
                                    .collect(Collectors.toList()))
                    .withCipherText(encodedPayload.getCipherText())
                    .withCipherTextNonce(encodedPayload.getCipherTextNonce());
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

        public Builder withRecipientKey(PublicKey publicKey) {
            this.recipientKeys.add(publicKey);
            return this;
        }

        public Builder withRecipientKeys(final List<PublicKey> recipientKeys) {
            this.recipientKeys.addAll(recipientKeys);
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

        public Builder withRecipientBox(byte[] newbox) {
            this.recipientBoxes.add(newbox);
            return this;
        }

        public EncodedPayload build() {
            List<RecipientBox> recipientBoxes =
                    this.recipientBoxes.stream().map(RecipientBox::from).collect(Collectors.toList());

            return new EncodedPayload(
                    senderKey, cipherText, cipherTextNonce, recipientBoxes, recipientNonce, recipientKeys);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodedPayload that = (EncodedPayload) o;
        return Objects.equals(senderKey, that.senderKey)
                && Arrays.equals(cipherText, that.cipherText)
                && Objects.equals(cipherTextNonce, that.cipherTextNonce)
                && Objects.equals(recipientBoxes, that.recipientBoxes)
                && Objects.equals(recipientNonce, that.recipientNonce)
                && Objects.equals(recipientKeys, that.recipientKeys);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(senderKey, cipherTextNonce, recipientBoxes, recipientNonce, recipientKeys);
        result = 31 * result + Arrays.hashCode(cipherText);
        return result;
    }
}
