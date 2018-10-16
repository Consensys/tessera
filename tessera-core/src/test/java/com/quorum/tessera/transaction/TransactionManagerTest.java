package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.api.model.ResendResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import com.quorum.tessera.util.Base64Decoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class TransactionManagerTest {
    
    private TransactionManager transactionManager;
    
    private PayloadEncoder payloadEncoder;
    
    private EncryptedTransactionDAO encryptedTransactionDAO;
    
    private PayloadPublisher payloadPublisher;
    
    private Enclave enclave;
    
    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        payloadPublisher = mock(PayloadPublisher.class);
        transactionManager = new TransactionManagerImpl(Base64Decoder.create(), payloadEncoder, encryptedTransactionDAO, payloadPublisher, enclave);
        
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, encryptedTransactionDAO, payloadPublisher, enclave);
    }
    
    @Test
    public void send() {
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        
        when(enclave.encryptPayload(any(), any(), any())).thenReturn(encodedPayloadWithRecipients);
        
        String sender = Base64.getEncoder().encodeToString("SENDER".getBytes());
        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        
        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        
        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setTo(receiver);
        sendRequest.setPayload(payload);
        
        SendResponse result = transactionManager.send(sendRequest);
        
        assertThat(result).isNotNull();
        
        verify(enclave).encryptPayload(any(), any(), any());
        verify(payloadEncoder).encode(encodedPayloadWithRecipients);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadPublisher).publishPayload(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
    }
    
    @Test
    public void delete() {
        
        String encodedKey = Base64.getEncoder().encodeToString("SOMEKEY".getBytes());
        
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey(encodedKey);
        transactionManager.delete(deleteRequest);
        
        verify(encryptedTransactionDAO).delete(any(MessageHash.class));
        
    }
    
    @Test
    public void storePayload() {
        
        byte[] input = "SOMEDATA".getBytes();
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        
        when(payloadEncoder.decodePayloadWithRecipients(input)).thenReturn(encodedPayloadWithRecipients);
        
        transactionManager.storePayload(input);
        
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).encode(encodedPayloadWithRecipients);
        verify(payloadEncoder).decodePayloadWithRecipients(input);
        
    }
    
    @Test
    public void resendAll() {
        
        EncryptedTransaction someTransaction = new EncryptedTransaction(mock(MessageHash.class), "someTransaction".getBytes());
        EncryptedTransaction someOtherTransaction = new EncryptedTransaction(mock(MessageHash.class), "someOtherTransaction".getBytes());
        
        when(encryptedTransactionDAO.retrieveAllTransactions())
                .thenReturn(Arrays.asList(someTransaction, someOtherTransaction));
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(encodedPayloadWithRecipients.getRecipientKeys())
                .thenReturn(Arrays.asList(PublicKey.from("PUBLICKEY".getBytes())));
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        
        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());
        
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(publicKeyData);
        resendRequest.setType(ResendRequestType.ALL);
        
        ResendResponse result = transactionManager.resend(resendRequest);
        assertThat(result).isNotNull();
        
        verify(encryptedTransactionDAO).retrieveAllTransactions();
        verify(payloadEncoder, times(2)).decodePayloadWithRecipients(any(byte[].class));
        verify(payloadPublisher, times(2)).publishPayload(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
    }
    
    @Test
    public void resendIndividualNoExistingTransactionFound() {
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.empty());
        
        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from(publicKeyData.getBytes());
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(new String(keyData));
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        
        try {
            transactionManager.resend(resendRequest);
            failBecauseExceptionWasNotThrown(TransactionNotFoundException.class);
        } catch (TransactionNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        }
        
    }
    
    @Test
    public void resendIndividual() {
        
        byte[] encodedPayloadData = "getRecipientKeys".getBytes();
        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        when(encryptedTransaction.getEncodedPayload()).thenReturn(encodedPayloadData);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientBoxes()).thenReturn(Arrays.asList("RECIPIENTBOX".getBytes()));
        
        byte[] encodedOutcome = "SUCCESS".getBytes();
        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from(publicKeyData.getBytes());
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        when(payloadEncoder.decodePayloadWithRecipients(encodedPayloadData, recipientKey))
                .thenReturn(encodedPayloadWithRecipients);
        
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn(encodedOutcome);
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(new String(keyData));
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        
        ResendResponse result = transactionManager.resend(resendRequest);
        
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);
        
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients(encodedPayloadData, recipientKey);
        verify(payloadEncoder).encode(any(EncodedPayloadWithRecipients.class));
    }
    
    @Test
    public void receive() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());
        
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        
        MessageHash messageHash = new MessageHash(keyData);
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        byte[] expectedOutcome = "Encrytpted payload".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class)))
                .thenReturn(expectedOutcome);
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);
        
        assertThat(receiveResponse).isNotNull();
        
        assertThat(receiveResponse.getPayload())
                .isEqualTo(expectedOutcome);
        
        verify(payloadEncoder, times(2)).decodePayloadWithRecipients(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }
    
    @Test
    public void receiveNoTransactionInDatabase() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());
        
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.empty());
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(TransactionNotFoundException.class);
        } catch (TransactionNotFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        }
        
    }
    
    @Test
    public void receiveNoRecipientKeyFound() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());
        
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        
        MessageHash messageHash = new MessageHash(keyData);
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class))).thenThrow(NaclException.class);
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(NoRecipientKeyFoundException.class);
        } catch (NoRecipientKeyFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
            verify(payloadEncoder).decodePayloadWithRecipients(any(byte[].class));
        }
        
    }
    
    @Test
    public void receiveHH() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());
        
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        
        MessageHash messageHash = new MessageHash(keyData);
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class)))
                .thenReturn(encodedPayloadWithRecipients)
                .thenReturn(null);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        byte[] expectedOutcome = "Encrytpted payload".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class)))
                .thenReturn(expectedOutcome);
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException ex) {
            verify(payloadEncoder, times(2)).decodePayloadWithRecipients(any(byte[].class));
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
            verify(enclave).getPublicKeys();
        }
        
    }
    
    @Test
    public void receiveNullRecipientThrowsNoRecipientKeyFound() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        
        MessageHash messageHash = new MessageHash(keyData);
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class))).thenThrow(NaclException.class);
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(NoRecipientKeyFoundException.class);
        } catch (NoRecipientKeyFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
            verify(payloadEncoder).decodePayloadWithRecipients(any(byte[].class));
        }
        
    }
    
    @Test
    public void receiveEmptyRecipientThrowsNoRecipientKeyFound() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo("");
        MessageHash messageHash = new MessageHash(keyData);
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class))).thenThrow(NaclException.class);
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(NoRecipientKeyFoundException.class);
        } catch (NoRecipientKeyFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
            verify(payloadEncoder).decodePayloadWithRecipients(any(byte[].class));
        }
        
    }
}
