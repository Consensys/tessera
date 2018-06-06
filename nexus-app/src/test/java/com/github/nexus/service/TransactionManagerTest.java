package com.github.nexus.service;

import com.github.nexus.dao.EncryptedTransactionDAO;
import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {

    private EncryptedTransactionDAO dao;

    private TransactionManager transactionManager;

    @Before
    public void init() {
        this.dao = mock(EncryptedTransactionDAO.class);
        this.transactionManager = new TransactionManagerImpl(dao);
    }

    @Test
    public void testDelete(){
        when(dao.delete(any())).thenReturn(true);
        transactionManager.delete(new MessageHash(new byte[0]));
        verify(dao, times(1)).delete(any());
    }

    @Test
    public void testRetrieveAllForRecipient(){
        transactionManager.retrieveAllForRecipient(new Key(new byte[0]));
    }

    @Test
    public void testRetrievePayload(){
        transactionManager.retrievePayload(new MessageHash(new byte[0]), new Key(new byte[0]));
    }

    @Test
    public void testRetrieve(){
        transactionManager.retrieve(new MessageHash(new byte[0]), new Key(new byte[0]));
    }

    @Test
    public void testStorePayloadFromOtherNode(){
        transactionManager.storePayloadFromOtherNode(new byte[0]);
    }

    @Test
    public void testEncryptPayload(){
        transactionManager.encryptPayload(new byte[0], new Key(new byte[0]), Collections.EMPTY_LIST);
    }
}
