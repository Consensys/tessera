package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;

import java.util.Collection;
import java.util.List;

public interface TransactionService {

    /**
     * Deletes a particular transaction from the enclave
     * Returns whether the desired state was achieved
     * i.e. {@code true} if the message no longer exists, {@code false} if the message still exists
     * <p>
     * Note that the method will return true if trying to delete a non-existent message,
     * since the desired state of the message no longer existing is satisfied
     *
     * @param hash The hash of the payload that should be deleted
     */
    void delete(MessageHash hash);

    /**
     * Retrieves all payloads that the provided key is a recipient to
     *
     * @param recipientPublicKey The key for which to search over
     * @return The list of all payloads that have the given key as a recipient
     */
    Collection<EncodedPayloadWithRecipients> retrieveAllForRecipient(Key recipientPublicKey);

    /**
     * Retrieves the message specified by the provided hash,
     * and checks the intended recipient is present in the recipient list
     * <p>
     * If the given recipient is present, then modifies the returned payload to only include
     * that recipient in the sealed box list so that it can be sent to the other nodes
     *
     * @param hash The hash of the message to retrieve
     * @return The encrypted payload if found
     * @throws NullPointerException         if the hash or intended recipient is null
     * @throws TransactionNotFoundException if the transaction is not found
     */
    EncodedPayloadWithRecipients retrievePayload(MessageHash hash);

    /**
     * Retrieves the message specified by the provided hash,
     * and uses the provided key as the sender of the message
     * <p>
     * Returns the unencrypted payload that is contained within so that
     * Quorum can process it
     *
     * @param hash   The hash of the message to retrieve
     * @param sender The public key of the sender to use (or {@code null} if we are the originator)
     * @return The encrypted payload if the sender keys match
     * @throws NullPointerException if the hash or sender is null
     * @throws RuntimeException     if the provided sender is not the original sender of the message
     */
    byte[] retrieveUnencryptedTransaction(MessageHash hash, Key sender);

    /**
     * Store the sealed payload in the database that was propagated to from another node
     *
     * @param payloadWithRecipients the decoded message that gets sent between nodes
     * @return the SHA3-513 hash of the encrypted message
     */
    MessageHash storeEncodedPayload(EncodedPayloadWithRecipients payloadWithRecipients);

    /**
     * Encrypts the payload using each of the given recipient keys and a random nonce
     * against the given sender's public key.
     * <p>
     * Produces a result that maps the given recipient key and nonce value against the resulting ciphertext
     *
     * @param message             The message to be encrypted
     * @param senderPublicKey     The public key of the sender
     * @param recipientPublicKeys The list of public keys that are recipients of the this message
     * @return A map of keys to nonces to cipher texts
     */
    EncodedPayloadWithRecipients encryptPayload(byte[] message, Key senderPublicKey, List<Key> recipientPublicKeys);

}
