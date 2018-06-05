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
    public byte[] send(){
        encryptedTransactionDAO.save(new EncryptedTransaction("someValue".getBytes(), "somePayload".getBytes()));
        return "mykey".getBytes();
    }

    @Override
    public void receive() {
        LOGGER.info("receive");
    }

    @Override
    public void delete() {
        LOGGER.info("delete");
    }

    @Override
    public void resend(){
        LOGGER.info("resend");
    }

    @Override
    public void push(){
        LOGGER.info("push");
    }
}
