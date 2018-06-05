package com.github.nexus.service;

public interface TransactionService {

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

}
