package com.github.nexus.service;

import com.github.nexus.dao.SomeDAO;
import com.github.nexus.entity.SomeEntity;

import javax.transaction.Transactional;
import java.util.logging.Logger;

@Transactional
public class TransactionServiceImpl implements TransactionService{

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());

    private SomeDAO someDAO;

    public TransactionServiceImpl(final SomeDAO someDAO) {
        this.someDAO = someDAO;
    }

    @Override
    public byte[] send(){
        someDAO.save(new SomeEntity("someValue"));
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
