package com.quorum.tessera.transaction.resend;

/** Handles resend requests where the response has one of our own keys as the sender */
public interface ResendManager {

    /**
     * Decodes, retrieves and/or creates an {@link com.quorum.tessera.data.EncryptedTransaction} based on currently
     * stored contents of the node
     *
     * @param message the message to be decoded and stored
     */
    void acceptOwnMessage(byte[] message);
}
