package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.nacl.Key;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;

import java.util.Optional;

public interface Enclave {

    /**
     * Deletes a particular transaction from the enclave
     * Returns whether the desired state was achieved
     * i.e. {@code true} if the message no longer exists, {@code false} if the message still exists
     * <p>
     * Note that the method will return true if trying to delete a non-existant message,
     * since the desired state of the message no longer existing is satisfied
     *
     * @param hashBytes The hash of the payload that should be deleted
     */
    void delete(byte[] hashBytes);

    /**
     * Retrieve a particular transaction
     * @param key hash key used to retrieve transaction
     * @param to the recipient of the transaction
     * @return the raw payload associated with the request.
     */
    byte[] receive(byte[] key, Optional<byte[]> to);


    /**
     *
     * @param sender
     * @param recipients
     * @param message
     * @return
     */
    MessageHash store(Optional<byte[]> sender, byte[][] recipients, byte[] message);

    MessageHash storePayload(byte[] encodedPayloadWithRecipients);

    void publishPayload(EncodedPayloadWithRecipients encodedPayload, Key recipient);

    void resendAll(byte[] recipientKey);

    /**
     * Fetch a transaction with the given hash
     * The transaction should have originated from this node and the
     * given key should appear in the recipients list
     *
     * @param hash the hash of the transaction to find
     * @param recipient the recipient of the transaction
     * @return The payload that was originally distributed to the node that managed the recipient
     */
    EncodedPayloadWithRecipients fetchTransactionForRecipient(MessageHash hash, Key recipient);
}
