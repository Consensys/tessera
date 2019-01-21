package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncryptedRawTransaction;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import com.quorum.tessera.util.Base64Decoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {
    
    private TransactionManager transactionManager;
    
    private PayloadEncoder payloadEncoder;
    
    private EncryptedTransactionDAO encryptedTransactionDAO;
    private EncryptedRawTransactionDAO encryptedRawTransactionDAO;
    
    private PayloadPublisher payloadPublisher;
    
    private Enclave enclave;
    
    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        payloadPublisher = mock(PayloadPublisher.class);
        transactionManager = new TransactionManagerImpl(Base64Decoder.create(), payloadEncoder, encryptedTransactionDAO,
            payloadPublisher, enclave, encryptedRawTransactionDAO);
        
    }
    
    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, encryptedTransactionDAO, payloadPublisher, enclave);
    }
    
    @Test
    public void send() {
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        
        when(enclave.encryptPayload(any(), any(), any())).thenReturn(encodedPayload);
        
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
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadPublisher,times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransaction() {

        EncodedPayload payload = mock(EncodedPayload.class);

        EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction(
            new MessageHash("HASH".getBytes()), "ENCRYPTED_PAYLOAD".getBytes(),
            "ENCRYPTED_KEY".getBytes(), "NONCE".getBytes(), "SENDER".getBytes()
        );

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
            .thenReturn(Optional.of(encryptedRawTransaction));

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any())).thenReturn(payload);

        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setTo(receiver);
        sendSignedRequest.setHash("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadPublisher).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransactionNoRawTransactionFoundException() {

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
            .thenReturn(Optional.empty());

        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());
        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setTo(receiver);
        sendSignedRequest.setHash("HASH".getBytes());

        try {
            transactionManager.sendSignedTransaction(sendSignedRequest);
            failBecauseExceptionWasNotThrown(TransactionNotFoundException.class);
        } catch (TransactionNotFoundException ex) {
            verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getForwardingKeys();
        }
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
        
        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        
        when(payloadEncoder.decode(input)).thenReturn(payload);
        
        transactionManager.storePayload(input);
        
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).decode(input);
        
    }
    
    @Test
    public void resendAll() {
        
        EncryptedTransaction someTransaction = new EncryptedTransaction(mock(MessageHash.class), "someTransaction".getBytes());
        EncryptedTransaction someOtherTransaction = new EncryptedTransaction(mock(MessageHash.class), "someOtherTransaction".getBytes());
        
        when(encryptedTransactionDAO.retrieveAllTransactions())
                .thenReturn(Arrays.asList(someTransaction, someOtherTransaction));
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payload.getRecipientKeys()).thenReturn(singletonList(PublicKey.from("PUBLICKEY".getBytes())));
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        
        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());
        
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(publicKeyData);
        resendRequest.setType(ResendRequestType.ALL);
        
        ResendResponse result = transactionManager.resend(resendRequest);
        assertThat(result).isNotNull();
        
        verify(encryptedTransactionDAO).retrieveAllTransactions();
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadPublisher, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
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

        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(encodedPayload, recipientKey)).thenReturn(encodedPayload);
        
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedOutcome);
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        
        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(new String(keyData));
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        
        ResendResponse result = transactionManager.resend(resendRequest);
        
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);
        
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(payloadEncoder).forRecipient(encodedPayload, recipientKey);
        verify(payloadEncoder).encode(any(EncodedPayload.class));
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
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
            .thenReturn(Optional.of(encryptedTransaction));
        
        byte[] expectedOutcome = "Encrypted payload".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);
        
        assertThat(receiveResponse).isNotNull();
        
        assertThat(receiveResponse.getPayload()).isEqualTo(expectedOutcome);
        
        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }
    
    @Test
    public void receiveNoTransactionInDatabase() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());
        
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        receiveRequest.setTo(recipient);
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());
        
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
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenThrow(NaclException.class);
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(NoRecipientKeyFoundException.class);
        } catch (NoRecipientKeyFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
            verify(payloadEncoder).decode(any(byte[].class));
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
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, null);
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload).thenReturn(null);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        byte[] expectedOutcome = "Encrypted payload".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        final Throwable throwable = catchThrowable(() -> transactionManager.receive(receiveRequest));

        assertThat(throwable).isInstanceOf(IllegalStateException.class);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }
    
    @Test
    public void receiveNullRecipientThrowsNoRecipientKeyFound() {
        
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(new String(keyData));
        
        MessageHash messageHash = new MessageHash(keyData);
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenThrow(NaclException.class);
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(NoRecipientKeyFoundException.class);
        } catch (NoRecipientKeyFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
            verify(payloadEncoder).decode(any(byte[].class));
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
        
        EncodedPayload payload = mock(EncodedPayload.class);
        
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenThrow(NaclException.class);
        
        try {
            transactionManager.receive(receiveRequest);
            failBecauseExceptionWasNotThrown(NoRecipientKeyFoundException.class);
        } catch (NoRecipientKeyFoundException ex) {
            verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
            verify(enclave).getPublicKeys();
            verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
            verify(payloadEncoder).decode(any(byte[].class));
        }
        
    }
}
