package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;

import java.util.List;

/**
 * Wrap an {@link EncodedPayload} with the recipients of the message so that the
 * sender can identify which sealed box is theirs
 */
public class EncodedPayloadWithRecipients {

    private final EncodedPayload encodedPayload;

    private final List<PublicKey> recipientKeys;

    public EncodedPayloadWithRecipients(final EncodedPayload encodedPayload, final List<PublicKey> recipientKeys) {
        this.encodedPayload = encodedPayload;
        this.recipientKeys = recipientKeys;
    }

    public EncodedPayload getEncodedPayload() {
        return encodedPayload;
    }

    public List<PublicKey> getRecipientKeys() {
        return recipientKeys;
    }

}
