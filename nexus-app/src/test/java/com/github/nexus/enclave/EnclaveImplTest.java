package com.github.nexus.enclave;

import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.nacl.Key;
import com.github.nexus.nacl.Nonce;
import com.github.nexus.node.PostDelegate;
import com.github.nexus.node.PartyInfoService;
import com.github.nexus.node.model.PartyInfo;
import com.github.nexus.transaction.PayloadEncoder;
import com.github.nexus.transaction.TransactionService;
import com.github.nexus.transaction.model.EncodedPayload;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EnclaveImplTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private PartyInfoService partyInfoService;

    @Mock
    private PayloadEncoder encoder;

    @Mock
    private PostDelegate postDelegate;

    private EnclaveImpl enclave;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        enclave = new EnclaveImpl(transactionService, partyInfoService,encoder, postDelegate);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(transactionService, encoder, postDelegate);
    }

    @Test
    public void testDelete() {
        doReturn(true).when(transactionService).delete(any(MessageHash.class));
        enclave.delete(new byte[0]);
        verify(transactionService).delete(any(MessageHash.class));
    }

    @Test
    public void testReceive() {
        enclave.receive(new byte[0], new byte[0]);
        verify(transactionService).retrieveUnencryptedTransaction(any(), any());
    }


    @Test
    public void testStore() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        enclave.store(new byte[0], new byte[0][0], new byte[0]);
        
        verify(transactionService).encryptPayload(any(), any(), any());
        
        verify(transactionService).storeEncodedPayload(payload);
    }

    @Test
    public void testStorePayload(){
    when(transactionService.storeEncodedPayload(any(EncodedPayloadWithRecipients.class)))
        .thenReturn(new MessageHash(new byte[0]));
        enclave.storePayload(new byte[0]);
        verify(encoder).decodePayloadWithRecipients(any());
        verify(transactionService).storeEncodedPayload(any());
    }
    
    @Ignore
    public void testStoreWithRecipientStuff() {

        EncodedPayloadWithRecipients payload = mock(EncodedPayloadWithRecipients.class);
        when(transactionService.encryptPayload(any(), any(), any())).thenReturn(payload);

        when(partyInfoService.getURLFromRecipientKey(any())).thenReturn("someurl.com");
        PartyInfo partyInfo = new PartyInfo("ownurl.com",Collections.emptySet(), Collections.emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        byte[][] recipients = new byte[1][1];
        recipients[0]  = new byte[] {'P'};
        
        enclave.store(new byte[0],recipients, new byte[0]);
        
        verify(transactionService).encryptPayload(any(), any(), any());
        
        verify(transactionService).storeEncodedPayload(payload);
    }

    @Test
    public void testStoreWithRecipientAndPublish(){
        EncodedPayload encodedPayload =
            new EncodedPayload(new Key(new byte[0]),
                new byte[0],
                new Nonce(new byte[0]),
                Arrays.asList("box1".getBytes(), "box2".getBytes()),
                new Nonce(new byte[0]));
        List<Key> recipientKeys =  Arrays.asList(new Key("somekey".getBytes()), new Key("key2".getBytes()));
        EncodedPayloadWithRecipients encodedPayloadWithRecipients =
            new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);

        Key recipientKey = new Key("somekey".getBytes());

        when(transactionService.encryptPayload(any(),any(),any())).thenReturn(encodedPayloadWithRecipients);
        when(transactionService.storeEncodedPayload(encodedPayloadWithRecipients))
            .thenReturn(new MessageHash("somehash".getBytes()));
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn("someurl.com");
        PartyInfo partyInfo = new PartyInfo("ownurl.com",Collections.emptySet(), Collections.emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        enclave.store(new byte[0],new byte[][]{"somekey".getBytes(), "key2".getBytes()}, new byte[0]);

        enclave.publishPayload(encodedPayloadWithRecipients, recipientKey);

        verify(transactionService).encryptPayload(any(), any(), any());
        verify(transactionService).storeEncodedPayload(encodedPayloadWithRecipients);

        verify(encoder, times(3)).encode(any(EncodedPayloadWithRecipients.class));

        verify(postDelegate, times(3)).doPost(any(),any(),any());

    }

    @Test
    public void testResendAll(){
        EncodedPayload encodedPayload =
            new EncodedPayload(new Key(new byte[0]),
                new byte[0],
                new Nonce(new byte[0]),
                Arrays.asList("box1".getBytes(), "box2".getBytes()),
                new Nonce(new byte[0]));
        List<Key> recipientKeys =  Arrays.asList(new Key("somekey".getBytes()), new Key("key2".getBytes()));
        EncodedPayloadWithRecipients encodedPayloadWithRecipients =
            new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);

        when(transactionService.retrieveAllForRecipient(any()))
            .thenReturn(Arrays.asList(encodedPayloadWithRecipients));

        Key recipientKey = new Key("somekey".getBytes());
        when(partyInfoService.getURLFromRecipientKey(recipientKey)).thenReturn("http://someurl.com");
        PartyInfo partyInfo = new PartyInfo("http://someurl.com",Collections.emptySet(), Collections.emptySet());
        when(partyInfoService.getPartyInfo()).thenReturn(partyInfo);

        enclave.resendAll("someKey".getBytes());

        verify(transactionService, times(1)).retrieveAllForRecipient(any());

        verify(encoder).encode(any(EncodedPayloadWithRecipients.class));

        verify(postDelegate, times(1)).doPost(any(),any(),any());

    }
}
