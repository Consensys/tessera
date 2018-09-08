package com.quorum.tessera.transaction.model;

import com.quorum.tessera.nacl.Key;

import java.util.Collections;
import java.util.List;

/**
 * Wrap an {@link EncodedPayload} with the recipients of the message
 * so that the sender can identify which sealed box is theirs
 */
public class EncodedPayloadWithRecipients {

    private final EncodedPayload encodedPayload;

    private final List<Key> recipientKeys;

    public EncodedPayloadWithRecipients(final EncodedPayload encodedPayload, final List<Key> recipientKeys) {
        this.encodedPayload = encodedPayload;
        this.recipientKeys = Collections.unmodifiableList(recipientKeys);
    }

    public EncodedPayload getEncodedPayload() {
        return encodedPayload;
    }

    public List<Key> getRecipientKeys() {
        return recipientKeys;
    }

}
