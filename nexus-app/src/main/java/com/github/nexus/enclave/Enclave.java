package com.github.nexus.enclave;

import com.github.nexus.enclave.keys.model.Key;

import java.util.Collection;
import java.util.Map;

public interface Enclave {

    /**
     * Deletes a particular transaction from the enclave
     * Returns whether the desired state was achieved
     * i.e. {@code true} if the message no longer exists, {@code false} if the message still exists
     *
     * Note that the method will return true if trying to delete a non-existant message,
     * since the desired state of the message no longer existing is satisfied
     *
     * @param hash The hash of the payload that should be deleted
     * @return States whether the delete operation was successful
     */
    boolean delete(byte[] hash);

    /**
     * Retrieves all payloads that the provided key is a recipient to
     *
     * @param recipientPublicKey The key for which to search over
     * @return The list of all payloads that have the given key as a recipient
     */
    Collection<String> retrieveAllForRecipient(Key recipientPublicKey);

    /**
     * Retrieves the message specified by the provided hash,
     * and checks the intended recipient is present in the recipient list
     *
     * @param hash The hash of the message to retrieve
     * @param intendedRecipient The public key of a recipient to check is present
     * @return The encrypted payload if the recipient is present
     * @throws NullPointerException if the hash or intended recipient is null
     * @throws RuntimeException if the provided recipient is not on the message recipient list
     */
    String retrievePayload(byte[] hash, Key intendedRecipient);

    /**
     * Retrieves the message specified by the provided hash,
     * and uses the provided key as the sender of the message
     *
     * @param hash The hash of the message to retrieve
     * @param sender The public key of the sender to use
     * @return The encrypted payload if the sender keys match
     * @throws NullPointerException if the hash or sender is null
     * @throws RuntimeException if the provided sender is not the original sender of the message
     */
    String retrieve(byte[] hash, Key sender);

    /**
     * Store the sealed payload in the database that was propagated to from another node
     *
     * @param sealedPayload the encrypted and encoded payload
     * @return The hash of the sealed payload
     */
    byte[] storePayloadFromOtherNode(byte[] sealedPayload);

    /**
     * Encrypts the payload using each of the given recipient keys and a random nonce
     * against the given sender's public key.
     *
     * Produces a result that maps the given recipient key and nonce value against the resulting ciphertext
     *
     * @param message The message to be encrypted
     * @param senderPublicKey The public key of the sender
     * @param recipientPublicKeys The list of public keys that are recipients of the this message
     * @return A map of keys to nonces to cipher texts
     */
    Map<Key, Map<byte[], byte[]>> encryptPayload(byte[] message, Key senderPublicKey, Collection<Key> recipientPublicKeys);

}
