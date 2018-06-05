package com.github.nexus.enclave;

import com.github.nexus.dao.EncryptedTransactionDAO;
import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnclaveImplTest {

    private EncryptedTransactionDAO dao;

    private Enclave enclave;

    @Before
    public void setUp(){
        this.dao = mock(EncryptedTransactionDAO.class);
        enclave = new EnclaveImpl(dao);
    }


    @Test
    public void testDelete(){
        when(dao.delete(any())).thenReturn(true);
        enclave.delete(new MessageHash(new byte[0]));
        verify(dao, times(1)).delete(any());
    }

    @Test
    public void testRetrieveAllForRecipient(){
        enclave.retrieveAllForRecipient(new Key(new byte[0]));
    }

    @Test
    public void testRetrievePayload(){
        enclave.retrievePayload(new MessageHash(new byte[0]), new Key(new byte[0]));
    }

    @Test
    public void testRetrieve(){
        enclave.retrieve(new MessageHash(new byte[0]), new Key(new byte[0]));
    }

    @Test
    public void testStorePayloadFromOtherNode(){
        enclave.storePayloadFromOtherNode(new byte[0]);
    }

    @Test
    public void testStore(){
        enclave.store(new byte[0], new byte[0][0], new byte[0]);
    }

    @Test
    public void testEncryptPayload(){
        enclave.encryptPayload(new byte[0], new Key(new byte[0]), Collections.EMPTY_LIST);
    }
}
