package com.github.nexus.service;

public interface TransactionService {

    /**
     *
     * @param from
     * @param recipients
     * @param payload
     * @return
     */
    byte[] send(byte[] from, byte[][] recipients, byte[] payload);


    /**
     *
     * @param key
     * @param to
     * @return
     */
    byte[] receive(byte[] key, byte[] to);

    void delete(byte[] key);

    byte[] push(byte[] payload);

}
