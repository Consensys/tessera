package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
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
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import com.quorum.tessera.util.Base64Decoder;
import java.util.Arrays;
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

        String payload = Base64.getEncoder().encodeToString("PAYLOAD".getBytes());

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

        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(publicKeyData);
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);
        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveAllTransactions();

    }



    @Test
    public void resendIndividual() {

        byte[] encodedPayloadData = "".getBytes();
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
        when(payloadEncoder.decodePayloadWithRecipients(encodedPayloadData,recipientKey))
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
        verify(payloadEncoder).decodePayloadWithRecipients(encodedPayloadData,recipientKey);
        verify(payloadEncoder).encode(any(EncodedPayloadWithRecipients.class));
    }

}
