package com.github.nexus.transaction;

import com.github.nexus.transaction.EncryptedTransactionDAO;
import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.TransactionService;
import com.github.nexus.transaction.TransactionServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionServiceTest {

    private EncryptedTransactionDAO dao;

    private TransactionService transactionService;

    @Before
    public void init() {
        this.dao = mock(EncryptedTransactionDAO.class);
        this.transactionService = new TransactionServiceImpl(dao);
    }

    @Test
    public void testDelete(){
        when(dao.delete(any())).thenReturn(true);
        transactionService.delete(new MessageHash(new byte[0]));
        verify(dao, times(1)).delete(any());
    }

    @Test
    public void testRetrieveAllForRecipient(){
        transactionService.retrieveAllForRecipient(new Key(new byte[0]));
    }

    @Test
    public void testRetrievePayload(){
        transactionService.retrievePayload(new MessageHash(new byte[0]), new Key(new byte[0]));
    }

    @Test
    public void testRetrieve(){
        transactionService.retrieve(new MessageHash(new byte[0]), new Key(new byte[0]));
    }

    @Test
    public void testStorePayloadFromOtherNode(){
        transactionService.storePayloadFromOtherNode(new byte[0]);
    }

    @Test
    public void testEncryptPayload(){
        transactionService.encryptPayload(new byte[0], new Key(new byte[0]), Collections.EMPTY_LIST);
    }
}
