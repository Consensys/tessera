package com.github.nexus.service;

import java.util.logging.Logger;
import javax.transaction.Transactional;

@Transactional
public class TransactionServiceImpl implements TransactionService{

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());

    @Override
    public void send(){
        LOGGER.info("send");
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
