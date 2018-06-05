package com.github.nexus.enclave;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;
import org.junit.Test;

import java.util.Collections;

public class EnclaveImplTest {

    private Enclave enclave = new EnclaveImpl();

    //TODO Please implement these tests


    @Test
    public void testDelete(){
        enclave.delete(new MessageHash(new byte[0]));
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
