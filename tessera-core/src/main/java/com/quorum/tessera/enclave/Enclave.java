package com.quorum.tessera.enclave;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;

import java.util.Optional;

public interface Enclave {

    /**
     * Deletes a particular transaction from the enclave
     * Returns whether the desired state was achieved
     * i.e. {@code true} if the message no longer exists, {@code false} if the message still exists
     * <p>
     * Note that the method will return true if trying to delete a non-existent message,
     * since the desired state of the message no longer existing is satisfied
     *
     * @param hashBytes The hash of the payload that should be deleted
     */
    void delete(byte[] hashBytes);

    /**
     * Retrieves the message specified by the provided hash,
     * and uses the provided key. If key is not provided,
     * method will use the default public key, which is public key of the current node
     * @param key The hash of payload to retrieve
     * @param to
     * @return
     */
    byte[] receive(byte[] key, Optional<byte[]> to);

    /**
     * Create an EncodedPayloadWithRecipients, then store this into the database,
     * also publish the the payload to all recipients
     * If sender public key is not provided, method will use default public key,
     * which is public key of the current node
     * @param sender Public key of the sender
     * @param recipients List of recipients for this transaction
     * @param message
     * @return The message hash returned after the transaction is  persisted.
     * This message hash can be used to retrieve the transaction.
     */
    MessageHash store(Optional<byte[]> sender, byte[][] recipients, byte[] message);

    /**
     * Decode raw payload from byte array to EncodedPayloadWithRecipients,
     * and persist to the database
     * @param encodedPayloadWithRecipients
     * @return The message hash returned after the transaction is  persisted.
     * This message hash can be used to retrieve the transaction
     */
    MessageHash storePayload(byte[] encodedPayloadWithRecipients);

    /**
     * Publish the encoded payload to the specified recipient.
     * This method will strip out irrelevant recipients and recipient boxes from the payload
     * before sending. If the recipient
     * @param encodedPayload Payload to send to recipient specified
     * @param recipient public key of the recipient to send payload to
     */
    void publishPayload(EncodedPayloadWithRecipients encodedPayload, Key recipient);

    /**
     * Retrieves all payloads that the provided key is a recipient to,
     * then publish the payload to all recipients in the recipient list of the payload
     * @param recipientKey The key for which to search over
     */
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
