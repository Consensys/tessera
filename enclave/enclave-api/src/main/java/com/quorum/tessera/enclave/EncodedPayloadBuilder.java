package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EncodedPayloadBuilder {

    private PublicKey senderKey;

    private byte[] cipherText;

    private byte[] cipherTextNonce;

    private final List<byte[]> recipientBoxes = new ArrayList<>();

    private byte[] recipientNonce;

    private final List<PublicKey> recipientKeys = new ArrayList<>();

    public static EncodedPayloadBuilder create() {
        return new EncodedPayloadBuilder();
    }

    private EncodedPayloadBuilder() {}

    public EncodedPayloadBuilder withSenderKey(final PublicKey senderKey) {
        this.senderKey = senderKey;
        return this;
    }

    public EncodedPayloadBuilder withCipherText(final byte[] cipherText) {
        this.cipherText = cipherText;
        return this;
    }

    public EncodedPayloadBuilder withRecipientKeys(final PublicKey... recipientKeys) {
        this.recipientKeys.addAll(Arrays.asList(recipientKeys));
        return this;
    }

    public EncodedPayloadBuilder withCipherTextNonce(final byte[] cipherTextNonce) {
        this.cipherTextNonce = cipherTextNonce;
        return this;
    }

    public EncodedPayloadBuilder withRecipientNonce(final byte[] recipientNonce) {
        this.recipientNonce = recipientNonce;
        return this;
    }

    public EncodedPayloadBuilder withRecipientBoxes(final List<byte[]> recipientBoxes) {
        this.recipientBoxes.addAll(recipientBoxes);
        return this;
    }

    public EncodedPayload build() {
        return new EncodedPayload(
                senderKey,
                cipherText,
                new Nonce(cipherTextNonce),
                recipientBoxes,
                new Nonce(recipientNonce),
                recipientKeys);
    }
}
