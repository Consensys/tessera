package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.service.TransactionManager;

import static java.util.Objects.requireNonNull;

public class EnclaveImpl implements Enclave {

    private final TransactionManager transactionManager;

    public EnclaveImpl(final TransactionManager transactionManager) {
        this.transactionManager = requireNonNull(transactionManager,"transaction service cannot be null");
    }

    @Override
    public boolean delete(final MessageHash hash) {
        return transactionManager.delete(hash);
    }

    @Override
    public byte[] send(byte[] from, byte[][] to, byte[] payload){
//        transactionManager.(new EncryptedTransaction("someValue".getBytes(), "somePayload".getBytes()));
        return "mykey".getBytes();
    }

    @Override
    public byte[] receive(byte[] key, byte[] to) {
//        transactionManager.retrieveAllTransactions();
        return "payload".getBytes();
    }

    @Override
    public byte[] store(byte[] sender, byte[][] recipients, byte[] message) {
        return new byte[0];
    }
}
