package com.github.nexus.service;

import com.github.nexus.dao.EncryptedTransactionDAO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
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
        transactionService.send();
        Mockito.verify(dao).save(any());
    }

    @Test
    public void testReceive(){
        transactionService.receive();
    }

    @Test
    public void testDelete(){
        transactionService.delete();
    }

    @Test
    public void testResend(){
        transactionService.resend();
    }

    @Test
    public void testPush(){
        transactionService.push();
    }
}
