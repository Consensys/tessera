package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;

public interface Enclave {

    /**
     * Deletes a particular transaction from the enclave
     * Returns whether the desired state was achieved
     * i.e. {@code true} if the message no longer exists, {@code false} if the message still exists
     * <p>
     * Note that the method will return true if trying to delete a non-existant message,
     * since the desired state of the message no longer existing is satisfied
     *
     * @param hash The hash of the payload that should be deleted
     * @return States whether the delete operation was successful
     */
    boolean delete(MessageHash hash);

    /**
     * Sends a new transaction to the enclave for storage and propagation to the provided list of recipients
     * @param from sender node identification.
     * @param recipients a list of the recipient nodes.
     * @param payload transaction payload data we wish to store.
     * @return a hash key. This key can be used to retrieve the submitted transaction
     */
    byte[] send(byte[] from, byte[][] recipients, byte[] payload);

    /**
     * Retrieve a particular transaction
     * @param key hash key used to retrieve transaction
     * @param to the recipient of the transaction
     * @return the raw payload associated with the request.
     */
    byte[] receive(byte[] key, byte[] to);


    /**
     *
     * @param sender
     * @param recipients
     * @param message
     * @return
     */
    MessageHash store(byte[] sender, byte[][] recipients, byte[] message);
}
