package com.github.nexus.service;

import org.junit.Test;

public class TransactionServiceTest {

    TransactionService transactionService = new TransactionServiceImpl();

    @Test
    public void testSend(){
        transactionService.send();
    }

    @Test
    public void testReceive(){
        transactionService.receive();
    }
}
