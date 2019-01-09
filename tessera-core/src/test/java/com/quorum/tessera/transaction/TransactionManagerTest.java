package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.*;
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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
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
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        
        when(enclave.encryptPayload(any(), any(), any())).thenReturn(encodedPayloadWithRecipients);
        when(payloadEncoder.forRecipient(any(EncodedPayloadWithRecipients.class), any(PublicKey.class)))
            .thenReturn(encodedPayloadWithRecipients);

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
        verify(payloadEncoder, times(2)).forRecipient(eq(encodedPayloadWithRecipients), any(PublicKey.class));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadPublisher, times(2)).publishPayload(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransaction() {

        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);

        EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction(
            new MessageHash("HASH".getBytes()), "ENCRYPTED_PAYLOAD".getBytes(),
            "ENCRYPTED_KEY".getBytes(), "NONCE".getBytes(), "SENDER".getBytes()
        );

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(
            Optional.of(encryptedRawTransaction));

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        when(encodedPayload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any())).thenReturn(encodedPayloadWithRecipients);

        String receiver = Base64.getEncoder().encodeToString("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setTo(receiver);
        sendSignedRequest.setHash("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any());
        verify(payloadEncoder).encode(encodedPayloadWithRecipients);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadPublisher).publishPayload(any(EncodedPayloadWithRecipients.class), any(PublicKey.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransactionNoRawTransactionFoundException() {

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(
            Optional.empty());

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
    public void storePayloadAsRecipient() {
        
        byte[] input = "SOMEDATA".getBytes();
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(encodedPayload);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        
        when(payloadEncoder.decodePayloadWithRecipients(input)).thenReturn(encodedPayloadWithRecipients);
        
        transactionManager.storePayload(input);
        
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).decodePayloadWithRecipients(input);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void storePayloadAsSenderWhenTxIsntPresent() {

        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final byte[] input = "SOMEDATA".getBytes();
        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(encodedPayload, new ArrayList<>());
        final byte[] newEncryptedMasterKey = "newbox".getBytes();

        when(encodedPayload.getRecipientBoxes()).thenReturn(new ArrayList<>());
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(payloadEncoder.decodePayloadWithRecipients(input)).thenReturn(encodedPayloadWithRecipients);
        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());
        when(enclave.createNewRecipientBox(any(), any())).thenReturn(newEncryptedMasterKey);
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn("updated".getBytes());


        transactionManager.storePayload(input);

        assertThat(encodedPayloadWithRecipients.getRecipientKeys()).containsExactly(senderKey);
        assertThat(encodedPayloadWithRecipients.getEncodedPayload().getRecipientBoxes()).containsExactly(newEncryptedMasterKey);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients(input);
        verify(payloadEncoder).encode(any(EncodedPayloadWithRecipients.class));
        verify(enclave).getPublicKeys();
        verify(enclave).createNewRecipientBox(any(), any());

    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresent() {

        final byte[] incomingData = "incomingData".getBytes();

        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload encodedPayload
            = new EncodedPayload(senderKey, "CIPHERTEXT".getBytes(), null, singletonList(recipientBox), null);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(encodedPayload, singletonList(recipientKey));

        final EncodedPayload existingEncodedPayload
            = new EncodedPayload(senderKey, "CIPHERTEXT".getBytes(), null, new ArrayList<>(), null);
        final EncodedPayloadWithRecipients existingEncodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(existingEncodedPayload, new ArrayList<>());

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decodePayloadWithRecipients(storedData)).thenReturn(existingEncodedPayloadWithRecipients);
        when(payloadEncoder.decodePayloadWithRecipients(incomingData)).thenReturn(encodedPayloadWithRecipients);
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn("updated".getBytes());

        transactionManager.storePayload(incomingData);

        assertThat(encodedPayloadWithRecipients.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayloadWithRecipients.getEncodedPayload().getRecipientBoxes()).containsExactly(recipientBox);

        verify(encryptedTransactionDAO).save(et);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients(storedData);
        verify(payloadEncoder).decodePayloadWithRecipients(incomingData);
        verify(payloadEncoder).encode(existingEncodedPayloadWithRecipients);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void storePayloadAsSenderWhenTxIsPresentAndRecipientExisted() {

        final byte[] incomingData = "incomingData".getBytes();

        final byte[] storedData = "SOMEDATA".getBytes();
        final EncryptedTransaction et = new EncryptedTransaction(null, storedData);
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final PublicKey recipientKey = PublicKey.from("RECIPIENT-KEY".getBytes());
        final byte[] recipientBox = "BOX".getBytes();

        final EncodedPayload encodedPayload
            = new EncodedPayload(senderKey, "CIPHERTEXT".getBytes(), null, singletonList(recipientBox), null);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(encodedPayload, singletonList(recipientKey));

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.of(et));
        when(payloadEncoder.decodePayloadWithRecipients(storedData)).thenReturn(encodedPayloadWithRecipients);
        when(payloadEncoder.decodePayloadWithRecipients(incomingData)).thenReturn(encodedPayloadWithRecipients);
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn("updated".getBytes());



        transactionManager.storePayload(incomingData);

        assertThat(encodedPayloadWithRecipients.getRecipientKeys()).containsExactly(recipientKey);
        assertThat(encodedPayloadWithRecipients.getEncodedPayload().getRecipientBoxes()).containsExactly(recipientBox);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients(storedData);
        verify(payloadEncoder).decodePayloadWithRecipients(incomingData);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientExists() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        final EncodedPayload innerPayload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        when(innerPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(innerPayload);
        when(encryptedTransactionDAO.retrieveAllTransactions()).thenReturn(singletonList(tx));
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        when(encodedPayloadWithRecipients.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(encodedPayloadWithRecipients, recipientKey)).thenReturn(new byte[0]);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveAllTransactions();
        verify(payloadEncoder).decodePayloadWithRecipients(encodedData);
        verify(payloadPublisher).publishPayload(any(EncodedPayloadWithRecipients.class), eq(senderKey));
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(encodedPayloadWithRecipients, recipientKey);
    }

    @Test
    public void resendAllWithNoValidTransactions() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();

        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(mock(EncodedPayload.class), emptyList());

        when(encryptedTransactionDAO.retrieveAllTransactions()).thenReturn(singletonList(tx));
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveAllTransactions();
        verify(payloadEncoder).decodePayloadWithRecipients(encodedData);
    }

    @Test
    public void resendAllWhereRequestedIsRecipient() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload innerPayload = mock(EncodedPayload.class);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients
            = new EncodedPayloadWithRecipients(innerPayload, singletonList(recipientKey));

        when(encryptedTransactionDAO.retrieveAllTransactions()).thenReturn(singletonList(tx));
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveAllTransactions();
        verify(payloadEncoder).decodePayloadWithRecipients(encodedData);
        verify(payloadPublisher).publishPayload(any(EncodedPayloadWithRecipients.class), eq(recipientKey));
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientDoesntExist() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        final EncodedPayload innerPayload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());

        when(innerPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayloadWithRecipients.getEncodedPayload()).thenReturn(innerPayload);
        when(encryptedTransactionDAO.retrieveAllTransactions()).thenReturn(singletonList(tx));
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class))).thenReturn(encodedPayloadWithRecipients);
        when(encodedPayloadWithRecipients.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(enclave.getPublicKeys()).thenReturn(emptySet());

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        final Throwable throwable = catchThrowable(() -> transactionManager.resend(resendRequest));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage(null);

        verify(encryptedTransactionDAO).retrieveAllTransactions();
        verify(payloadEncoder).decodePayloadWithRecipients(encodedData);
        verify(enclave).getPublicKeys();
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

        final byte[] encodedPayloadData = "getRecipientKeys".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(null, encodedPayloadData);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
            .thenReturn(Optional.of(encryptedTransaction));
        
        final EncodedPayload encodedPayload
            = new EncodedPayload(null, null, null, singletonList("RECIPIENTBOX".getBytes()), null);

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey recipientKey = PublicKey.from("PUBLICKEY".getBytes());
        
        EncodedPayloadWithRecipients payloadWithRecipients = new EncodedPayloadWithRecipients(encodedPayload, null);

        when(payloadEncoder.decodePayloadWithRecipients(encodedPayloadData)).thenReturn(payloadWithRecipients);
        when(payloadEncoder.forRecipient(payloadWithRecipients, recipientKey)).thenReturn(payloadWithRecipients);

        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn(encodedOutcome);

        final String messageHashb64 = Base64.getEncoder().encodeToString("KEY".getBytes());

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(messageHashb64);
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);
        
        ResendResponse result = transactionManager.resend(resendRequest);
        
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);
        
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients(encodedPayloadData);
        verify(payloadEncoder).forRecipient(payloadWithRecipients, recipientKey);
        verify(payloadEncoder).encode(payloadWithRecipients);
    }

    @Test
    public void resendIndividualAsSender() {

        final byte[] encodedPayloadData = "getRecipientKeys".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(null, encodedPayloadData);

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        final EncodedPayload encodedPayload
            = new EncodedPayload(senderKey, null, null, singletonList("RECIPIENTBOX".getBytes()), null);

        EncodedPayloadWithRecipients payloadWithRecipients = new EncodedPayloadWithRecipients(encodedPayload, null);


        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
            .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decodePayloadWithRecipients(encodedPayloadData)).thenReturn(payloadWithRecipients);
        when(payloadEncoder.encode(any(EncodedPayloadWithRecipients.class))).thenReturn(encodedOutcome);
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(any(), any())).thenReturn(new byte[0]);

        final String messageHashb64 = Base64.getEncoder().encodeToString("KEY".getBytes());

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(messageHashb64);
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);

        final ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);

        final ArgumentCaptor<EncodedPayloadWithRecipients> captor
            = ArgumentCaptor.forClass(EncodedPayloadWithRecipients.class);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decodePayloadWithRecipients(encodedPayloadData);
        verify(payloadEncoder).encode(captor.capture());
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(any(), any());

        assertThat(captor.getValue().getRecipientKeys()).containsExactly(recipientKey);
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
        
        byte[] expectedOutcome = "Encrypted payload".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class)))
                .thenReturn(expectedOutcome);
        
        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
        
        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);
        
        assertThat(receiveResponse).isNotNull();
        
        assertThat(receiveResponse.getPayload()).isEqualTo(expectedOutcome);
        
        verify(payloadEncoder).decodePayloadWithRecipients(any(byte[].class));
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
        
        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, null);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = mock(EncodedPayloadWithRecipients.class);
        
        when(payloadEncoder.decodePayloadWithRecipients(any(byte[].class)))
                .thenReturn(encodedPayloadWithRecipients)
                .thenReturn(null);
        
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        
        byte[] expectedOutcome = "Encrypted payload".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class), any(PublicKey.class)))
                .thenReturn(expectedOutcome);
        
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
