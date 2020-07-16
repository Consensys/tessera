package com.quorum.tessera.transaction;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.*;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.exception.KeyNotFoundException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.util.Base64Decoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private PartyInfoService partyInfoService;

    private ResendManager resendManager;

    private Enclave enclave;

    private MessageHashFactory messageHashFactory = MessageHashFactory.create();

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);

        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        partyInfoService = mock(PartyInfoService.class);
        this.resendManager = mock(ResendManager.class);

        transactionManager =
                new TransactionManagerImpl(
                        Base64Decoder.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        partyInfoService,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        1000);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(payloadEncoder, encryptedTransactionDAO, partyInfoService, enclave);
    }

    @Test
    public void send() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any())).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendAlsoWithPublishCallbackCoverage() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any())).thenReturn(encodedPayload);

        doAnswer(
                        invocation -> {
                            Callable callable = invocation.getArgument(1);
                            callable.call();
                            return mock(EncryptedTransaction.class);
                        })
                .when(encryptedTransactionDAO)
                .save(any(EncryptedTransaction.class), any(Callable.class));

        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendWithDuplicateRecipients() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any())).thenReturn(encodedPayload);
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));

        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(encodedPayload);

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransaction() {

        EncodedPayload payload = mock(EncodedPayload.class);

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        new MessageHash("HASH".getBytes()),
                        "ENCRYPTED_PAYLOAD".getBytes(),
                        "ENCRYPTED_KEY".getBytes(),
                        "NONCE".getBytes(),
                        "SENDER".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedRawTransaction));
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransactionWithCallbackCoverage() {

        EncodedPayload payload = mock(EncodedPayload.class);

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        new MessageHash("HASH".getBytes()),
                        "ENCRYPTED_PAYLOAD".getBytes(),
                        "ENCRYPTED_KEY".getBytes(),
                        "NONCE".getBytes(),
                        "SENDER".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedRawTransaction));

        doAnswer(
                        invocation -> {
                            Callable callable = invocation.getArgument(1);
                            callable.call();
                            return mock(EncryptedTransaction.class);
                        })
                .when(encryptedTransactionDAO)
                .save(any(EncryptedTransaction.class), any(Callable.class));

        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any());
        verify(payloadEncoder).encode(payload);
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendSignedTransactionWithDuplicateRecipients() {

        EncodedPayload payload = mock(EncodedPayload.class);

        EncryptedRawTransaction encryptedRawTransaction =
                new EncryptedRawTransaction(
                        new MessageHash("HASH".getBytes()),
                        "ENCRYPTED_PAYLOAD".getBytes(),
                        "ENCRYPTED_KEY".getBytes(),
                        "NONCE".getBytes(),
                        "SENDER".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedRawTransaction));
        when(payloadEncoder.forRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));
        when(enclave.encryptPayload(any(RawTransaction.class), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
    }

    @Test
    public void sendSignedTransactionNoRawTransactionFoundException() {

        when(encryptedRawTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());
        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

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

        MessageHash messageHash = mock(MessageHash.class);

        transactionManager.delete(messageHash);

        verify(encryptedTransactionDAO).delete(messageHash);
    }

    @Test
    public void storePayloadAsRecipient() {
        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        transactionManager.storePayload(payload);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).encode(payload);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void storePayloadWhenWeAreSender() {
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getRecipientBoxes()).thenReturn(new ArrayList<>());
        when(encodedPayload.getRecipientKeys()).thenReturn(new ArrayList<>());

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));

        transactionManager.storePayload(encodedPayload);

        verify(resendManager).acceptOwnMessage(encodedPayload);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientExists() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);

        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(payload, recipientKey)).thenReturn(new byte[0]);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(senderKey));
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(payload, recipientKey);
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void resendAllWithNoValidTransactions() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();

        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(emptyList());

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
    }

    @Test
    public void resendAllWhereRequestedIsRecipient() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(payload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadEncoder).forRecipient(payload, recipientKey);
        verify(partyInfoService).publishPayload(any(EncodedPayload.class), eq(recipientKey));
    }

    @Test
    public void resendAllWhereRequestedIsRecipientThenPublishedPayloadDoesNotContainDataForOtherRecipients() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);

        final List<PublicKey> recipients = new ArrayList<>();
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());
        recipients.add(recipientKey);
        recipients.add(anotherRecipient);

        final List<byte[]> recipientBoxes = new ArrayList<>();
        final byte[] recipientBox = "box1".getBytes();
        final byte[] anotherRecipientBox = "box2".getBytes();
        recipientBoxes.add(recipientBox);
        recipientBoxes.add(anotherRecipientBox);

        when(payload.getRecipientKeys()).thenReturn(recipients);
        when(payload.getRecipientBoxes()).thenReturn(recipientBoxes);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        EncodedPayload prunedPayload = mock(EncodedPayload.class);
        when(prunedPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
        when(prunedPayload.getRecipientBoxes()).thenReturn(singletonList(recipientBox));
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(prunedPayload);

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        verify(partyInfoService).publishPayload(eq(prunedPayload), eq(recipientKey));

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).forRecipient(payload, recipientKey);
        verify(payloadEncoder).decode(encodedData);
    }

    @Test
    public void resendAllWhereRequestedIsSenderThenPublishedPayloadIsNotPruned() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);

        final List<byte[]> recipientBoxes = new ArrayList<>();
        final byte[] recipientBox = "box1".getBytes();
        recipientBoxes.add(recipientBox);

        final PublicKey localKey = PublicKey.from("LOCAL_KEY".getBytes());
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final List<PublicKey> recipients = new ArrayList<>();

        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(recipients);
        when(payload.getRecipientBoxes()).thenReturn(recipientBoxes);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(enclave.getPublicKeys()).thenReturn(singleton(localKey));

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        verify(partyInfoService).publishPayload(eq(payload), eq(senderKey));
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(payload, localKey);
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientDoesntExist() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());

        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payload.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(enclave.getPublicKeys()).thenReturn(emptySet());

        final ResendRequest resendRequest = new ResendRequest();
        resendRequest.setPublicKey(senderKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.ALL);

        final Throwable throwable = catchThrowable(() -> transactionManager.resend(resendRequest));

        assertThat(throwable)
                .isInstanceOf(KeyNotFoundException.class)
                .hasMessage("No key found as recipient of message Q0lQSEVSVEVYVA==");

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(1)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWithOnePayloadAndOneRecipientThenPublishPayloadExceptionIsCaught() {

        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        List<EncryptedTransaction> allDbTransactions = Collections.singletonList(encryptedTransaction);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(allDbTransactions);
        when(encryptedTransactionDAO.transactionCount()).thenReturn((long) allDbTransactions.size());

        byte[] transactionBytes = "TRANSACTION".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(transactionBytes);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(any())).thenReturn(encodedPayload);

        byte[] publicKeyBytes = "PUBLICKEY".getBytes();
        String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        ResendRequest resendRequest = mock(ResendRequest.class);
        when(resendRequest.getPublicKey()).thenReturn(publicKeyEncoded);
        when(resendRequest.getType()).thenReturn(ResendRequestType.ALL);

        when(payloadEncoder.forRecipient(eq(encodedPayload), any(PublicKey.class))).thenReturn(encodedPayload);

        doThrow(new PublishPayloadException("msg")).when(partyInfoService).publishPayload(encodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(partyInfoService).publishPayload(encodedPayload, publicKey);
        verify(payloadEncoder).decode(any(byte[].class));
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
    }

    @Test
    public void resendAllIfTwoPayloadsAndFirstThrowsExceptionThenSecondIsStillPublished() {
        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        EncryptedTransaction otherEncryptedTransaction = mock(EncryptedTransaction.class);
        List<EncryptedTransaction> allDbTransactions = Arrays.asList(encryptedTransaction, otherEncryptedTransaction);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(allDbTransactions);
        when(encryptedTransactionDAO.transactionCount()).thenReturn((long) allDbTransactions.size());

        byte[] transactionBytes = "TRANSACTION".getBytes();
        byte[] otherTransactionBytes = "OTHER_TRANSACTION".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(transactionBytes);
        when(otherEncryptedTransaction.getEncodedPayload()).thenReturn(otherTransactionBytes);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload otherEncodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(transactionBytes)).thenReturn(encodedPayload);
        when(payloadEncoder.decode(otherTransactionBytes)).thenReturn(otherEncodedPayload);

        byte[] publicKeyBytes = "PUBLICKEY".getBytes();
        String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));
        when(otherEncodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        ResendRequest resendRequest = mock(ResendRequest.class);
        when(resendRequest.getPublicKey()).thenReturn(publicKeyEncoded);
        when(resendRequest.getType()).thenReturn(ResendRequestType.ALL);

        when(payloadEncoder.forRecipient(eq(encodedPayload), any(PublicKey.class))).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(eq(otherEncodedPayload), any(PublicKey.class)))
                .thenReturn(otherEncodedPayload);

        doThrow(new PublishPayloadException("msg")).when(partyInfoService).publishPayload(encodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(partyInfoService).publishPayload(encodedPayload, publicKey);
        verify(partyInfoService).publishPayload(otherEncodedPayload, publicKey);
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
    }

    @Test
    public void resendAllTwoPayloadsAndTwoRecipientsAllThrowExceptionButAllStillPublished() {
        EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        EncryptedTransaction otherEncryptedTransaction = mock(EncryptedTransaction.class);
        List<EncryptedTransaction> allDbTransactions = Arrays.asList(encryptedTransaction, otherEncryptedTransaction);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(allDbTransactions);
        when(encryptedTransactionDAO.transactionCount()).thenReturn((long) allDbTransactions.size());

        byte[] transactionBytes = "TRANSACTION".getBytes();
        byte[] otherTransactionBytes = "OTHER_TRANSACTION".getBytes();
        when(encryptedTransaction.getEncodedPayload()).thenReturn(transactionBytes);
        when(otherEncryptedTransaction.getEncodedPayload()).thenReturn(otherTransactionBytes);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload otherEncodedPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode(transactionBytes)).thenReturn(encodedPayload);
        when(payloadEncoder.decode(otherTransactionBytes)).thenReturn(otherEncodedPayload);

        byte[] publicKeyBytes = "PUBLICKEY".getBytes();
        String publicKeyEncoded = Base64.getEncoder().encodeToString(publicKeyBytes);
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));
        when(otherEncodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        when(payloadEncoder.forRecipient(encodedPayload, publicKey)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(otherEncodedPayload, publicKey)).thenReturn(otherEncodedPayload);

        ResendRequest resendRequest = mock(ResendRequest.class);
        when(resendRequest.getPublicKey()).thenReturn(publicKeyEncoded);
        when(resendRequest.getType()).thenReturn(ResendRequestType.ALL);

        doThrow(new PublishPayloadException("msg")).when(partyInfoService).publishPayload(encodedPayload, publicKey);

        doThrow(new PublishPayloadException("msg"))
                .when(partyInfoService)
                .publishPayload(otherEncodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(partyInfoService).publishPayload(encodedPayload, publicKey);
        verify(partyInfoService).publishPayload(otherEncodedPayload, publicKey);
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(partyInfoService, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void resendIndividualNoExistingTransactionFound() {

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

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

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList("RECIPIENTBOX".getBytes()));

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey recipientKey = PublicKey.from("PUBLICKEY".getBytes());

        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(encodedPayload, recipientKey)).thenReturn(encodedPayload);

        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedOutcome);

        final String messageHashb64 = Base64.getEncoder().encodeToString("KEY".getBytes());

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey(messageHashb64);
        resendRequest.setPublicKey(recipientKey.encodeToBase64());
        resendRequest.setType(ResendRequestType.INDIVIDUAL);

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPayload()).contains(encodedOutcome);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(payloadEncoder).forRecipient(encodedPayload, recipientKey);
        verify(payloadEncoder).encode(encodedPayload);
    }

    @Test
    public void resendIndividualAsSender() {

        final byte[] encodedPayloadData = "getRecipientKeys".getBytes();
        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(null, encodedPayloadData);

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getRecipientBoxes()).thenReturn(singletonList("RECIPIENTBOX".getBytes()));
        when(encodedPayload.getRecipientKeys()).thenReturn(new ArrayList<>());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(payloadEncoder.encode(any(EncodedPayload.class))).thenReturn(encodedOutcome);
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

        final ArgumentCaptor<EncodedPayload> captor = ArgumentCaptor.forClass(EncodedPayload.class);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(payloadEncoder).encode(captor.capture());
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(any(), any());
        assertThat(captor.getValue().getRecipientKeys()).containsExactly(recipientKey);
    }

    @Test
    public void receive() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        String recipient = Base64.getEncoder().encodeToString("recipient".getBytes());

        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(keyData);
        when(receiveRequest.getTransactionHash()).thenReturn(messageHash);
        when(receiveRequest.getRecipient()).thenReturn(Optional.of(mock(PublicKey.class)));

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

        assertThat(receiveResponse.getUnencryptedTransactionData()).isEqualTo(expectedOutcome);

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void receiveNoTransactionInDatabase() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        PublicKey recipient = PublicKey.from("recipient".getBytes());

        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("KEY".getBytes());

        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        when(receiveRequest.getTransactionHash()).thenReturn(messageHash);
        when(receiveRequest.getRecipient()).thenReturn(Optional.of(recipient));

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

        final byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        PublicKey recipient = PublicKey.from("recipient".getBytes());

        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("KEY".getBytes());

        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        when(receiveRequest.getTransactionHash()).thenReturn(messageHash);
        when(receiveRequest.getRecipient()).thenReturn(Optional.of(recipient));

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenThrow(EncryptorException.class);

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
        PublicKey recipient = PublicKey.from("recipient".getBytes());

        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn("KEY".getBytes());

        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        when(receiveRequest.getRecipient()).thenReturn(Optional.of(recipient));
        when(receiveRequest.getTransactionHash()).thenReturn(messageHash);

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

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("KEY".getBytes());
        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        when(receiveRequest.getRecipient()).thenReturn(Optional.empty());
        when(receiveRequest.getTransactionHash()).thenReturn(transactionHash);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(transactionHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenThrow(EncryptorException.class);

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
        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("KEY".getBytes());
        when(receiveRequest.getTransactionHash()).thenReturn(transactionHash);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(transactionHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenThrow(EncryptorException.class);

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
    public void storeRaw() {
        byte[] sender = "SENDER".getBytes();
        RawTransaction rawTransaction =
                new RawTransaction(
                        "CIPHERTEXT".getBytes(),
                        "SomeKey".getBytes(),
                        new Nonce("nonce".getBytes()),
                        PublicKey.from(sender));

        when(enclave.encryptRawPayload(any(), any())).thenReturn(rawTransaction);

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        StoreRawRequest sendRequest = mock(StoreRawRequest.class);
        when(sendRequest.getSender()).thenReturn(PublicKey.from(sender));
        when(sendRequest.getPayload()).thenReturn(payload);

        MessageHash expectedHash = messageHashFactory.createFromCipherText("CIPHERTEXT".getBytes());

        StoreRawResponse result = transactionManager.store(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getHash().getHashBytes()).containsExactly(expectedHash.getHashBytes());

        verify(enclave).encryptRawPayload(eq(payload), eq(PublicKey.from(sender)));
        verify(encryptedRawTransactionDAO)
                .save(
                        argThat(
                                et -> {
                                    assertThat(et.getEncryptedKey()).containsExactly("SomeKey".getBytes());
                                    assertThat(et.getEncryptedPayload()).containsExactly("CIPHERTEXT".getBytes());
                                    assertThat(et.getHash()).isEqualTo(expectedHash);
                                    assertThat(et.getNonce()).containsExactly("nonce".getBytes());
                                    assertThat(et.getSender()).containsExactly(sender);
                                    return true;
                                }));
    }

    @Test
    public void constructWithLessArgs() {
        final MockServiceLocator serviceLocator = (MockServiceLocator) ServiceLocator.create();

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setApp(AppType.P2P);
        config.setServerConfigs(Arrays.asList(serverConfig));

        serviceLocator.setServices(
                Stream.of(config, partyInfoService, enclave, partyInfoService).collect(Collectors.toSet()));

        TransactionManager tm =
                new TransactionManagerImpl(
                        encryptedTransactionDAO,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        partyInfoService,
                        1000);

        assertThat(tm).isNotNull();
    }

    @Test
    public void publishDoesNotPublishToSender() {

        TransactionManagerImpl impl = TransactionManagerImpl.class.cast(transactionManager);
        EncodedPayload transaction = mock(EncodedPayload.class);

        PublicKey someKey = mock(PublicKey.class);

        List<PublicKey> recipients = Arrays.asList(someKey);

        when(enclave.getPublicKeys()).thenReturn(singleton(someKey));

        impl.publish(recipients, transaction);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void isSenderThrowsOnMissingTransaction() {

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("DUMMY_TRANSACTION".getBytes());

        when(encryptedTransactionDAO.retrieveByHash(transactionHash)).thenReturn(Optional.empty());

        final Throwable throwable = catchThrowable(() -> transactionManager.isSender(transactionHash));

        assertThat(throwable)
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Message with hash RFVNTVlfVFJBTlNBQ1RJT04= was not found");

        verify(encryptedTransactionDAO).retrieveByHash(transactionHash);
    }

    @Test
    public void isSenderReturnsFalseIfSenderNotFoundInPublicKeys() {
        final MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("DUMMY_TRANSACTION".getBytes());

        final byte[] input = "SOMEDATA".getBytes();

        final EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        when(encryptedTransaction.getEncodedPayload()).thenReturn(input);

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        PublicKey sender = mock(PublicKey.class);
        when(encodedPayload.getSenderKey()).thenReturn(sender);

        when(encryptedTransactionDAO.retrieveByHash(transactionHash)).thenReturn(Optional.of(encryptedTransaction));

        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);

        when(enclave.getPublicKeys()).thenReturn(emptySet());

        final boolean isSender = transactionManager.isSender(transactionHash);

        assertThat(isSender).isFalse();

        verify(enclave).getPublicKeys();
        verify(payloadEncoder).decode(input);
        verify(encryptedTransactionDAO).retrieveByHash(transactionHash);
    }

    @Test
    public void isSenderReturnsTrueIfSender() {

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("DUMMY_TRANSACTION".getBytes());

        final byte[] input = "SOMEDATA".getBytes();
        final EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        when(encryptedTransaction.getEncodedPayload()).thenReturn(input);

        final PublicKey senderKey = mock(PublicKey.class);

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encryptedTransactionDAO.retrieveByHash(transactionHash)).thenReturn(Optional.of(encryptedTransaction));

        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);

        when(enclave.getPublicKeys()).thenReturn(Set.of(senderKey));

        final boolean isSender = transactionManager.isSender(transactionHash);

        assertThat(isSender).isTrue();

        verify(enclave).getPublicKeys();
        verify(payloadEncoder).decode(input);
        verify(encryptedTransactionDAO).retrieveByHash(transactionHash);
    }

    @Test
    public void getParticipantsThrowsOnMissingTransaction() {

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("DUMMY_TRANSACTION".getBytes());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        final Throwable throwable = catchThrowable(() -> transactionManager.getParticipants(transactionHash));

        assertThat(throwable)
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Message with hash RFVNTVlfVFJBTlNBQ1RJT04= was not found");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void getParticipantsReturnsAllRecipients() {

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("DUMMY_TRANSACTION".getBytes());

        final PublicKey senderKey = mock(PublicKey.class);
        final PublicKey recipientKey = mock(PublicKey.class);

        final byte[] input = "SOMEDATA".getBytes();
        final EncryptedTransaction encryptedTransaction = mock(EncryptedTransaction.class);
        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getRecipientKeys()).thenReturn(List.of(senderKey, recipientKey));
        when(encryptedTransaction.getEncodedPayload()).thenReturn(input);

        when(encryptedTransactionDAO.retrieveByHash(transactionHash)).thenReturn(Optional.of(encryptedTransaction));

        when(payloadEncoder.decode(input)).thenReturn(encodedPayload);

        final List<PublicKey> participants = transactionManager.getParticipants(transactionHash);

        assertThat(participants).containsExactlyInAnyOrder(senderKey, recipientKey);

        verify(payloadEncoder).decode(input);
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
    }

    @Test
    public void defaultPublicKey() {
        transactionManager.defaultPublicKey();
        verify(enclave).defaultPublicKey();
    }

    @Test
    public void publish() {

        PublicKey reipcient = mock(PublicKey.class);
        List<PublicKey> recipients = List.of(reipcient);
        EncodedPayload payload = mock(EncodedPayload.class);

        when(enclave.getPublicKeys()).thenReturn(Set.of(mock(PublicKey.class)));
        when(payloadEncoder.forRecipient(payload, reipcient)).thenReturn(payload);

        TransactionManagerImpl.class.cast(transactionManager).publish(recipients, payload);

        verify(enclave).getPublicKeys();
        verify(payloadEncoder).forRecipient(payload, reipcient);
        verify(partyInfoService).publishPayload(payload, reipcient);
    }
}
