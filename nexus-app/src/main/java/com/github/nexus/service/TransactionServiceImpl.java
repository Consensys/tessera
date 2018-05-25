package com.github.nexus.service;

import java.util.logging.Logger;

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
}
