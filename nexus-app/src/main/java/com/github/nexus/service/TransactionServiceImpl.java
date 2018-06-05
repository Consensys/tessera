package com.github.nexus.service;

import com.github.nexus.dao.EncryptedTransactionDAO;
import com.github.nexus.entity.EncryptedTransaction;

import javax.transaction.Transactional;
import java.util.logging.Logger;

@Transactional
public class TransactionServiceImpl implements TransactionService{

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());

    private EncryptedTransactionDAO encryptedTransactionDAO;

    public TransactionServiceImpl(final EncryptedTransactionDAO encryptedTransactionDAO) {
        this.encryptedTransactionDAO = encryptedTransactionDAO;
    }

    @Override
    public byte[] send(byte[] from, byte[][] to, byte[] payload){
        encryptedTransactionDAO.save(new EncryptedTransaction("someValue".getBytes(), "somePayload".getBytes()));
        return "mykey".getBytes();
    }

    @Override
    public byte[] receive(byte[] key, byte[] to) {
        LOGGER.info("receive");
        encryptedTransactionDAO.retrieveAllTransactions();
        return "payload".getBytes();
    }
}
