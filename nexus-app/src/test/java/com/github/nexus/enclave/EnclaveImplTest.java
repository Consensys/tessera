package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.service.TransactionManager;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnclaveImplTest {

    private TransactionManager txService;

    private Enclave enclave;

    @Before
    public void setUp(){
        this.txService = mock(TransactionManager.class);
        enclave = new EnclaveImpl(txService);
    }

    @Test
    public void testDelete(){
        doReturn(true).when(txService).delete(any(MessageHash.class));

        enclave.delete(new MessageHash(new byte[0]));

        verify(txService).delete(any(MessageHash.class));
    }

    @Test
    public void testSend(){
        enclave.send(new byte[0], new byte[0][0], new byte[0]);
    }

    @Test
    public void testReceive(){
        enclave.receive(new byte[0], new byte[0]);
    }

    @Test
    public void testStore(){
        enclave.store(new byte[0], new byte[0][0], new byte[0]);
    }

}
