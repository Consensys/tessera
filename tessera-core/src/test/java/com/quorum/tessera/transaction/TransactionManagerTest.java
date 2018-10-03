package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.Base64Decoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

public class TransactionManagerTest {
        
    private PayloadEncoder payloadEncoder;
    
    private Base64Decoder base64Decoder = Base64Decoder.create();
    
    private TransactionManagerImpl transactionManager;
    
    private TransactionServiceImpl transactionService;
    
    @Before
    public void onSetup() {
        payloadEncoder = mock(PayloadEncoder.class);
        this.transactionService = mock(TransactionServiceImpl.class);
        transactionManager = new TransactionManagerImpl(base64Decoder, payloadEncoder,transactionService);
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder,transactionService);
    }
    
    @Test
    public void testSend() {
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom("bXlwdWJsaWNrZXk=");
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");
        
        when(transactionService.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));
        
        SendResponse sendResponse = transactionManager.send(sendRequest);
        
        verify(transactionService, times(1)).store(any(), any(), any());
        
        assertThat(sendResponse).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(sendResponse.getKey()))).isEqualTo("SOMEKEY");
        
    }
    
    @Test
    public void testSendWithEmptySender() {
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");
        
        when(transactionService.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));
        
        SendResponse sendResponse = transactionManager.send(sendRequest);
        
        verify(transactionService, times(1)).store(any(), any(), any());
        
        assertThat(sendResponse).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(sendResponse.getKey()))).isEqualTo("SOMEKEY");
    }
    
    @Test
    public void testReceive() {
        doReturn("SOME DATA".getBytes()).when(transactionService).receive(any(), any());
        
        ReceiveRequest request = new ReceiveRequest();
        request.setTo("cmVjaXBpZW50MQ==");
        request.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        
        ReceiveResponse response = transactionManager.receive(request);
        
        verify(transactionService).receive(any(), any());
        
        assertThat(response).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(response.getPayload()))).isEqualTo("SOME DATA");
        
    }
    
    @Test
    public void testReceiveWithNoToField() {
        
        doReturn("SOME DATA".getBytes()).when(transactionService).receive(any(), any());
        
        ReceiveRequest request = new ReceiveRequest();
        request.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        
        ReceiveResponse response = transactionManager.receive(request);
        
        verify(transactionService).receive(any(), any());
        
        assertThat(response).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(response.getPayload()))).isEqualTo("SOME DATA");
    }
    
    @Test
    public void testDelete() {
        
        String key = Base64.getEncoder().encodeToString("HELLOW".getBytes());
        
        DeleteRequest request = new DeleteRequest();
        request.setKey(key);
        
        List<MessageHash> deletedKeys = new ArrayList<>();
        
        doAnswer((Answer) (iom) -> {
            deletedKeys.add(iom.getArgument(0));
            return null;
        }).when(transactionService).delete(any(MessageHash.class));
        
        transactionManager.delete(request);

        verify(transactionService).delete(any(MessageHash.class));
        
        assertThat(deletedKeys).hasSize(1);
        
    }
    
    @Test
    public void testResendAll() {
        
        ResendRequest request = new ResendRequest();
        
        request.setType(ResendRequestType.ALL);
        request.setPublicKey("mypublickey");
        request.setKey("mykey");
        
        Optional<byte[]> result = transactionManager.resendAndEncode(request);
        
        assertThat(result).isNotPresent();
        
        byte[] decodedKey = Base64.getDecoder().decode(request.getPublicKey());
        
        verify(transactionService).resendAll(decodedKey);
        
    }
    
    @Test
    public void testResendIndividual() {
        
        ResendRequest request = new ResendRequest();
        
        request.setType(ResendRequestType.INDIVIDUAL);
        request.setPublicKey("mypublickey");
        request.setKey(Base64.getEncoder().encodeToString("mykey".getBytes()));
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        byte[] encodedPayload = "RESUILT".getBytes();
        
        when(payloadEncoder.encode(encodedPayloadWithRecipients)).thenReturn(encodedPayload);
        
        when(transactionService.fetchTransactionForRecipient(any(), any())).thenReturn(encodedPayloadWithRecipients);
        
        Optional<byte[]> result = transactionManager.resendAndEncode(request);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(encodedPayload);
        
        verify(transactionService).fetchTransactionForRecipient(any(), any());
        verify(payloadEncoder).encode(encodedPayloadWithRecipients);
        
    }
    
    @Test
    public void receiveAndEncode() {
        
        String key = Base64.getEncoder().encodeToString("I LOVE SPARROWS".getBytes());
        String to = Base64.getEncoder().encodeToString("Frank Bough".getBytes());
        
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);
        receiveRequest.setTo(to);
        
        byte[] enclaveResponse = Base64.getDecoder().decode("DATA");
        
        when(transactionService.receive(any(), any())).thenReturn(enclaveResponse);
        
        String result = transactionManager.receiveAndEncode(receiveRequest);
        
        assertThat(result).isNotNull();
        
        assertThat(result).isEqualTo(Base64.getEncoder().encodeToString(enclaveResponse));
        
        verify(transactionService).receive(any(), any());
        
    }
    
    @Test
    public void receiveAndEncodeEmptyTo() {
        
        String key = Base64.getEncoder().encodeToString("I LOVE SPARROWS".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);
        receiveRequest.setTo("");
        byte[] enclaveResponse = Base64.getDecoder().decode("DATA");
        
        when(transactionService.receive(any(), any())).thenReturn(enclaveResponse);
        
        String result = transactionManager.receiveAndEncode(receiveRequest);
        
        assertThat(result).isNotNull();
        
        assertThat(result).isEqualTo(Base64.getEncoder().encodeToString(enclaveResponse));
        
        verify(transactionService).receive(any(), any());
        
    }
    
    @Test
    public void storeAndEncodeKey() {
        
        String sender = Base64.getEncoder().encodeToString("Bod".getBytes());
        String recipientKeys = Base64.getEncoder().encodeToString("recipientKeys".getBytes());
        byte[] payload = "PAYLOAD".getBytes();
        
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("messageHash".getBytes());
        
        when(transactionService.store(any(), any(), any())).thenReturn(messageHash);
        
        transactionManager.storeAndEncodeKey(sender, recipientKeys, payload);
        
        verify(transactionService).store(any(), any(), any());
        
    }
    
    
    @Test
    public void storeAndEncodeKeyNullRecipientKey() {
        
        String sender = Base64.getEncoder().encodeToString("Bod".getBytes());
        String recipientKeys = null;
        byte[] payload = "PAYLOAD".getBytes();
        
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("messageHash".getBytes());
        
        when(transactionService.store(any(), any(), any())).thenReturn(messageHash);
        
        transactionManager.storeAndEncodeKey(sender, recipientKeys, payload);
        
        verify(transactionService).store(any(), any(), any());
        
    }
    
    @Test
    public void storePayload() {
        
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("messageHash".getBytes());
        
        byte[] payload = "PAYLOAD".getBytes();
        
        when(transactionService.storePayload(payload)).thenReturn(messageHash);
        
        transactionManager.storePayload(payload);
        
        verify(transactionService).storePayload(payload);
        
    }
    
    @Test
    public void deleteKey() {
        
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        
        transactionManager.deleteKey(key);
        

        verify(transactionService).delete(any(MessageHash.class));
        
    }
    
    @Test
    public void receiveRaw() {
    
        String hash = Base64.getEncoder().encodeToString("hash".getBytes());
        String recipientKey = Base64.getEncoder().encodeToString("recipientKey".getBytes());

        transactionManager.receiveRaw(hash, recipientKey);
        
        verify(transactionService).receive(any(),any());

    }

    @Test
    public void testReceiveNullTo() {
        
        String hash = Base64.getEncoder().encodeToString("hash".getBytes());
        
        when(transactionService.receive(any(), any())).thenReturn("RESULT".getBytes());
        
        ReceiveResponse result = transactionManager.receive(hash, null);
        
        assertThat(result.getPayload())
                .isEqualTo(Base64.getEncoder().encodeToString("RESULT".getBytes()));
        
        verify(transactionService).receive(any(), any());
        
        
    }
    
        @Test
    public void testReceiveEmptyTo() {
        
        String hash = Base64.getEncoder().encodeToString("hash".getBytes());
        
        when(transactionService.receive(any(), any())).thenReturn("RESULT".getBytes());
        
        ReceiveResponse result = transactionManager.receive(hash, "");
        
        assertThat(result.getPayload())
                .isEqualTo(Base64.getEncoder().encodeToString("RESULT".getBytes()));
        
        verify(transactionService).receive(any(), any());
        
        
    }
}
