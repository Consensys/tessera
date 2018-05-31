package com.github.nexus.service;

import com.github.nexus.dao.SomeDAO;
import com.github.nexus.entity.SomeEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;

public class TransactionServiceTest {

    @Mock
    SomeDAO dao;

    TransactionService transactionService;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
        transactionService = new TransactionServiceImpl(dao);
        Mockito.doNothing().when(dao).save(any(SomeEntity.class));
    }

    @Test
    public void testSend(){
        transactionService.send();
        Mockito.verify(dao, Mockito.times(1)).save(any());

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
