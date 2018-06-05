package com.github.nexus.service;

import com.github.nexus.dao.EncryptedTransactionDAO;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        transactionService.send(new byte[0], new byte[0][0], new byte[0]);
        verify(dao, times((1))).save(any());
    }

    @Test
    public void testReceive(){
        transactionService.receive(new byte[0], new byte[0]);
        verify(dao, times(1)).retrieveAllTransactions();
    }
}
