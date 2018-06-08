package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.transaction.PayloadEncoder;
import com.github.nexus.transaction.TransactionService;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnclaveImplTest {

    @Mock
    private TransactionService txService;

    @Mock
    private PayloadEncoder encoder;

    private EnclaveImpl enclave;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        enclave = new EnclaveImpl(txService, encoder);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(txService, encoder);
    }

    @Test
    public void testDelete() {
        doReturn(true).when(txService).delete(any(MessageHash.class));

        enclave.delete(new MessageHash(new byte[0]));

        verify(txService).delete(any(MessageHash.class));
    }

    @Test
    public void testSend() {
        enclave.send(new byte[0], new byte[0][0], new byte[0]);
    }

    @Test
    public void testReceive() {
        enclave.receive(new byte[0], new byte[0]);
        verify(txService).retrieveUnencryptedTransaction(any(), any());
    }

    @Test
    public void testStore() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(txService.encryptPayload(any(), any(), any())).thenReturn(payload);

        enclave.store(new byte[0], new byte[0][0], new byte[0]);
        
        verify(txService).encryptPayload(any(), any(), any());
        
        verify(txService).storeEncodedPayload(payload);
    }
    
        @Test
    public void testStoreWithRecipientStuyff() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(txService.encryptPayload(any(), any(), any())).thenReturn(payload);

        byte[][] recipients = new byte[1][1];
        recipients[0]  = new byte[] {'P'};
        
        enclave.store(new byte[0],recipients, new byte[0]);
        
        verify(txService).encryptPayload(any(), any(), any());
        
        verify(txService).storeEncodedPayload(payload);
    }

}
