package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.TransactionService;

import static java.util.Objects.requireNonNull;

public class EnclaveImpl implements Enclave {

    private final TransactionService transactionService;

    public EnclaveImpl(final TransactionService transactionService) {
        this.transactionService = requireNonNull(transactionService,"transaction service cannot be null");
    }

    @Override
    public boolean delete(final MessageHash hash) {
        return transactionService.delete(hash);
    }

    @Override
    public byte[] send(byte[] from, byte[][] to, byte[] payload){
//        transactionService.(new EncryptedTransaction("someValue".getBytes(), "somePayload".getBytes()));
        return "mykey".getBytes();
    }

    @Override
    public byte[] receive(byte[] key, byte[] to) {
//        transactionService.retrieveAllTransactions();
        return "payload".getBytes();
    }

    @Override
    public byte[] store(byte[] sender, byte[][] recipients, byte[] message) {
        return new byte[0];
    }
}
