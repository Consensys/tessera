package com.quorum.tessera;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveMediator;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.PayloadEncoder;
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

public class EnclaveMediatorTest {
    
    private Enclave enclave;
    
    private PayloadEncoder payloadEncoder;
    
    private Base64Decoder base64Decoder = Base64Decoder.create();
    
    private EnclaveMediator enclaveMediator;
    
    @Before
    public void onSetup() {
        this.enclave = mock(Enclave.class);
        payloadEncoder = mock(PayloadEncoder.class);
        enclaveMediator = new EnclaveMediator(enclave, base64Decoder, payloadEncoder);
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave, payloadEncoder);
    }
    
    @Test
    public void testSend() {
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom("bXlwdWJsaWNrZXk=");
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");
        
        when(enclave.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));
        
        SendResponse sendResponse = enclaveMediator.send(sendRequest);
        
        verify(enclave, times(1)).store(any(), any(), any());
        
        assertThat(sendResponse).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(sendResponse.getKey()))).isEqualTo("SOMEKEY");
        
    }
    
    @Test
    public void testSendWithEmptySender() {
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setTo(new String[]{"cmVjaXBpZW50MQ=="});
        sendRequest.setPayload("Zm9v");
        
        when(enclave.store(any(), any(), any())).thenReturn(new MessageHash("SOMEKEY".getBytes()));
        
        SendResponse sendResponse = enclaveMediator.send(sendRequest);
        
        verify(enclave, times(1)).store(any(), any(), any());
        
        assertThat(sendResponse).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(sendResponse.getKey()))).isEqualTo("SOMEKEY");
    }
    
    @Test
    public void testReceive() {
        doReturn("SOME DATA".getBytes()).when(enclave).receive(any(), any());
        
        ReceiveRequest request = new ReceiveRequest();
        request.setTo("cmVjaXBpZW50MQ==");
        request.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        
        ReceiveResponse response = enclaveMediator.receive(request);
        
        verify(enclave).receive(any(), any());
        
        assertThat(response).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(response.getPayload()))).isEqualTo("SOME DATA");
        
    }
    
    @Test
    public void testReceiveWithNoToField() {
        
        doReturn("SOME DATA".getBytes()).when(enclave).receive(any(), any());
        
        ReceiveRequest request = new ReceiveRequest();
        request.setKey("ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=");
        
        ReceiveResponse response = enclaveMediator.receive(request);
        
        verify(enclave).receive(any(), any());
        
        assertThat(response).isNotNull();
        assertThat(new String(Base64Decoder.create().decode(response.getPayload()))).isEqualTo("SOME DATA");
    }
    
    @Test
    public void testDelete() {
        
        String key = Base64.getEncoder().encodeToString("HELLOW".getBytes());
        
        DeleteRequest request = new DeleteRequest();
        request.setKey(key);
        
        List<byte[]> deletedKeys = new ArrayList<>();
        
        doAnswer((Answer) (iom) -> {
            deletedKeys.add(iom.getArgument(0));
            return null;
        }).when(enclave).delete(any(byte[].class));
        
        enclaveMediator.delete(request);
        
        verify(enclave, times(1)).delete(any(byte[].class));
        
        assertThat(deletedKeys).hasSize(1);
        
    }
    
    @Test
    public void testResendAll() {
        
        ResendRequest request = new ResendRequest();
        
        request.setType(ResendRequestType.ALL);
        request.setPublicKey("mypublickey");
        request.setKey("mykey");
        
        Optional<byte[]> result = enclaveMediator.resendAndEncode(request);
        
        assertThat(result).isNotPresent();
        
        byte[] decodedKey = Base64.getDecoder().decode(request.getPublicKey());
        
        verify(enclave).resendAll(decodedKey);
        
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
        
        when(enclave.fetchTransactionForRecipient(any(), any())).thenReturn(encodedPayloadWithRecipients);
        
        Optional<byte[]> result = enclaveMediator.resendAndEncode(request);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(encodedPayload);
        
        verify(enclave).fetchTransactionForRecipient(any(), any());
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
        
        when(enclave.receive(any(), any())).thenReturn(enclaveResponse);
        
        String result = enclaveMediator.receiveAndEncode(receiveRequest);
        
        assertThat(result).isNotNull();
        
        assertThat(result).isEqualTo(Base64.getEncoder().encodeToString(enclaveResponse));
        
        verify(enclave).receive(any(), any());
        
    }
    
    @Test
    public void receiveAndEncodeEmptyTo() {
        
        String key = Base64.getEncoder().encodeToString("I LOVE SPARROWS".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);
        receiveRequest.setTo("");
        byte[] enclaveResponse = Base64.getDecoder().decode("DATA");
        
        when(enclave.receive(any(), any())).thenReturn(enclaveResponse);
        
        String result = enclaveMediator.receiveAndEncode(receiveRequest);
        
        assertThat(result).isNotNull();
        
        assertThat(result).isEqualTo(Base64.getEncoder().encodeToString(enclaveResponse));
        
        verify(enclave).receive(any(), any());
        
    }
    
    @Test
    public void storeAndEncodeKey() {
        
        String sender = Base64.getEncoder().encodeToString("Bod".getBytes());
        String recipientKeys = Base64.getEncoder().encodeToString("recipientKeys".getBytes());
        byte[] payload = "PAYLOAD".getBytes();
        
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("messageHash".getBytes());
        
        when(enclave.store(any(), any(), any())).thenReturn(messageHash);
        
        enclaveMediator.storeAndEncodeKey(sender, recipientKeys, payload);
        
        verify(enclave).store(any(), any(), any());
        
    }
    
    
    @Test
    public void storeAndEncodeKeyNullRecipientKey() {
        
        String sender = Base64.getEncoder().encodeToString("Bod".getBytes());
        String recipientKeys = null;
        byte[] payload = "PAYLOAD".getBytes();
        
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("messageHash".getBytes());
        
        when(enclave.store(any(), any(), any())).thenReturn(messageHash);
        
        enclaveMediator.storeAndEncodeKey(sender, recipientKeys, payload);
        
        verify(enclave).store(any(), any(), any());
        
    }
    
    @Test
    public void storePayload() {
        
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("messageHash".getBytes());
        
        byte[] payload = "PAYLOAD".getBytes();
        
        when(enclave.storePayload(payload)).thenReturn(messageHash);
        
        enclaveMediator.storePayload(payload);
        
        verify(enclave).storePayload(payload);
        
    }
    
    @Test
    public void deleteKey() {
        
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        
        enclaveMediator.deleteKey(key);
        
        verify(enclave).delete("KEY".getBytes());
        
    }
    
    @Test
    public void receiveRaw() {
    
        String hash = Base64.getEncoder().encodeToString("hash".getBytes());
        String recipientKey = Base64.getEncoder().encodeToString("recipientKey".getBytes());
                
        
        enclaveMediator.receiveRaw(hash, recipientKey);
        
        verify(enclave).receive(any(),any());
        
        
    }
    
    
        
    @Test
    public void testReceiveNullTo() {
        
        String hash = Base64.getEncoder().encodeToString("hash".getBytes());
        
        when(enclave.receive(any(), any())).thenReturn("RESULT".getBytes());
        
        ReceiveResponse result = enclaveMediator.receive(hash, null);
        
        assertThat(result.getPayload())
                .isEqualTo(Base64.getEncoder().encodeToString("RESULT".getBytes()));
        
        verify(enclave).receive(any(), any());
        
        
    }
    
        @Test
    public void testReceiveEmptyTo() {
        
        String hash = Base64.getEncoder().encodeToString("hash".getBytes());
        
        when(enclave.receive(any(), any())).thenReturn("RESULT".getBytes());
        
        ReceiveResponse result = enclaveMediator.receive(hash, "");
        
        assertThat(result.getPayload())
                .isEqualTo(Base64.getEncoder().encodeToString("RESULT".getBytes()));
        
        verify(enclave).receive(any(), any());
        
        
    }
}
