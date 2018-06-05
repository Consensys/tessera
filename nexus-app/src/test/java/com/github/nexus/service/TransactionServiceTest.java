package com.github.nexus.service;

import com.github.nexus.dao.EncryptedTransactionDAO;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TransactionServiceTest {

    private EncryptedTransactionDAO dao;

    private TransactionService transactionService;

    @Before
    public void init(){
        this.dao = mock(EncryptedTransactionDAO.class);
        transactionService = new TransactionServiceImpl(dao);
    }

    @Test
    public void testSend(){

    }

    @Test
    public void testReceive(){
    }

    @Test
    public void testDelete(){
    }

    @Test
    public void testResend(){
    }

    @Test
    public void testPush(){
    }
}
