package com.quorum.tessera.transaction;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.*;
import com.quorum.tessera.data.*;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.EncryptorException;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.transaction.exception.RecipientKeyNotFoundException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Codec;
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

    private PayloadPublisher payloadPublisher;

    private ResendManager resendManager;

    private Enclave enclave;

    private MessageHashFactory messageHashFactory = MessageHashFactory.create();

    private PrivacyHelper privacyHelper;

    private BatchPayloadPublisher batchPayloadPublisher;

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);

        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        payloadPublisher = mock(PayloadPublisher.class);
        this.resendManager = mock(ResendManager.class);
        this.privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO, true);
        batchPayloadPublisher = mock(BatchPayloadPublisher.class);
        transactionManager =
                new TransactionManagerImpl(
                        Base64Codec.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        payloadPublisher,
                        batchPayloadPublisher,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        privacyHelper,
                        1000);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(
                payloadEncoder,
                encryptedTransactionDAO,
                payloadPublisher,
                enclave,
                resendManager,
                batchPayloadPublisher);
    }

    @Test
    public void send() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void sendAlsoWithPublishCallbackCoverage() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);

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

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
        verify(batchPayloadPublisher).publishPayload(any(), anyList());
    }

    @Test
    public void sendWithDuplicateRecipients() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any(), any(), any())).thenReturn(encodedPayload);
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(), any(), any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
    }
    /*
           doAnswer(invocation -> {
               Callable callable = invocation.getArgument(1);
               callable.call();
               return mock(EncryptedTransaction.class);
           }).when(encryptedTransactionDAO).save(any(EncryptedTransaction.class),any(Callable.class));


           when(payloadEncoder.forRecipient(any(EncodedPayload.class),any(PublicKey.class))).thenReturn(encodedPayload);
    */
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

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any(), any(), any(), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), any(), any(), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
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

        when(enclave.encryptPayload(any(RawTransaction.class), any(), any(), any(), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), any(), any(), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
        verify(batchPayloadPublisher).publishPayload(any(), anyList());
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

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));
        when(enclave.encryptPayload(any(RawTransaction.class), any(), any(), any(), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), any(), any(), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(enclave, times(2)).getPublicKeys();
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
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
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
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void storePayloadWhenWeAreSenderWithPrivateStateConsensus() {
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final byte[] input = "SOMEDATA".getBytes();

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);
        when(encodedPayload.getSenderKey()).thenReturn(senderKey);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(encodedPayload.getCipherTextNonce()).thenReturn(null);
        when(encodedPayload.getRecipientBoxes()).thenReturn(emptyList());
        when(encodedPayload.getRecipientNonce()).thenReturn(null);
        when(encodedPayload.getRecipientKeys()).thenReturn(emptyList());
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(encodedPayload.getAffectedContractTransactions()).thenReturn(emptyMap());
        when(encodedPayload.getExecHash()).thenReturn(new byte[0]);

        when(enclave.getPublicKeys()).thenReturn(singleton(senderKey));

        transactionManager.storePayload(encodedPayload);

        verify(resendManager).acceptOwnMessage(encodedPayload);

        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Test
    public void storePayloadAsRecipientWithPrivateStateConsensus() {

        byte[] input = "SOMEDATA".getBytes();

        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        transactionManager.storePayload(payload);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(payloadEncoder).encode(payload);
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Test
    public void storePayloadAsRecipientWithAffectedContractTxsButPsvFlagMismatched() {

        final byte[] input = "SOMEDATA".getBytes();
        final byte[] affectedContractPayload = "SOMEOTHERDATA".getBytes();
        final PublicKey senderKey = PublicKey.from("sender".getBytes());

        final EncodedPayload payload = mock(EncodedPayload.class);
        final EncryptedTransaction affectedContractTx = mock(EncryptedTransaction.class);
        final EncodedPayload affectedContractEncodedPayload = mock(EncodedPayload.class);

        Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ=="),
                SecurityHash.from("securityHash".getBytes()));

        when(affectedContractTx.getEncodedPayload()).thenReturn(input);
        when(affectedContractTx.getHash())
                .thenReturn(
                        new MessageHash(
                                new TxHash(
                                                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==")
                                        .getBytes()));
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(affectedContractEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(affectedContractEncodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(senderKey));

        // when(payloadEncoder.decode(input)).thenReturn(payload);

        when(encryptedTransactionDAO.findByHashes(any())).thenReturn(List.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(payload);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO, times(0)).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).findByHashes(any());
        // verify(payloadEncoder, times(1)).decode(any());
    }

    @Test
    public void storePayloadSenderNotGenuineACOTHNotFound() {
        final byte[] input = "SOMEDATA".getBytes();
        final byte[] affectedContractPayload = "SOMEOTHERDATA".getBytes();
        final PublicKey senderKey = PublicKey.from("sender".getBytes());

        final EncodedPayload payload = mock(EncodedPayload.class);
        final EncryptedTransaction affectedContractTx = mock(EncryptedTransaction.class);
        final EncodedPayload affectedContractEncodedPayload = mock(EncodedPayload.class);

        final TxHash txHash =
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

        Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(txHash, SecurityHash.from("securityHash".getBytes()));
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSr5J5hQ=="),
                SecurityHash.from("bogus".getBytes()));

        when(affectedContractTx.getEncodedPayload()).thenReturn(input);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(affectedContractEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(affectedContractEncodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(senderKey));

        when(encryptedTransactionDAO.findByHashes(List.of(new MessageHash(txHash.getBytes()))))
                .thenReturn(List.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(payload);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO, times(0)).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).findByHashes(any());
    }

    @Test
    public void storePayloadSenderNotInRecipientList() {
        final byte[] input = "SOMEDATA".getBytes();
        final byte[] affectedContractPayload = "SOMEOTHERDATA".getBytes();
        final PublicKey senderKey = PublicKey.from("sender".getBytes());
        final PublicKey someOtherKey = PublicKey.from("otherKey".getBytes());

        final EncodedPayload payload = mock(EncodedPayload.class);
        final EncryptedTransaction affectedContractTx = mock(EncryptedTransaction.class);
        final EncodedPayload affectedContractEncodedPayload = mock(EncodedPayload.class);

        final TxHash txHash =
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==");

        Map<TxHash, SecurityHash> affectedContractTransactionHashes = new HashMap<>();
        affectedContractTransactionHashes.put(txHash, SecurityHash.from("securityHash".getBytes()));
        affectedContractTransactionHashes.put(
                new TxHash("bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSr5J5hQ=="),
                SecurityHash.from("bogus".getBytes()));

        when(affectedContractTx.getEncodedPayload()).thenReturn(input);
        when(affectedContractTx.getHash()).thenReturn(new MessageHash(txHash.getBytes()));
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(affectedContractEncodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedContractTransactionHashes);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(affectedContractEncodedPayload.getRecipientKeys()).thenReturn(Arrays.asList(someOtherKey));

        when(encryptedTransactionDAO.findByHashes(any())).thenReturn(List.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(payload);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO, times(0)).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).findByHashes(any());
    }

    @Test
    public void storePayloadPsvWithInvalidSecurityHashes() {

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        when(enclave.findInvalidSecurityHashes(any(), any()))
                .thenReturn(singleton(new TxHash("invalidHash".getBytes())));

        assertThatExceptionOfType(PrivacyViolationException.class)
                .describedAs("There are privacy violation for psv")
                .isThrownBy(() -> transactionManager.storePayload(payload))
                .withMessageContaining("Invalid security hashes identified for PSC TX");

        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Test
    public void storePayloadWithInvalidSecurityHashesIgnoreIfNotPsv() {

        final byte[] input = "SOMEDATA".getBytes();

        Map<TxHash, SecurityHash> affectedTx =
                Map.of(TxHash.from("invalidHash".getBytes()), SecurityHash.from("security".getBytes()));

        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getSenderKey()).thenReturn(PublicKey.from("sender".getBytes()));
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getCipherTextNonce()).thenReturn(new Nonce("nonce".getBytes()));

        RecipientBox recipientBox = mock(RecipientBox.class);
        when(recipientBox.getData()).thenReturn("box1".getBytes());

        when(payload.getRecipientBoxes()).thenReturn(List.of(recipientBox));
        when(payload.getRecipientNonce()).thenReturn(new Nonce("recipientNonce".getBytes()));
        when(payload.getRecipientKeys()).thenReturn(singletonList(PublicKey.from("recipient".getBytes())));
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedTx);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());

        ArgumentCaptor<EncodedPayload> payloadCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        when(enclave.findInvalidSecurityHashes(any(), any()))
                .thenReturn(singleton(new TxHash("invalidHash".getBytes())));

        transactionManager.storePayload(payload);

        verify(payloadEncoder).encode(payloadCaptor.capture());
        EncodedPayload sanitisedPayload = payloadCaptor.getValue();

        // Assert that the invalid ACOTH had been removed
        assertThat(sanitisedPayload.getAffectedContractTransactions().get(TxHash.from("invalidHash".getBytes())))
                .isNull();

        verify(encryptedTransactionDAO).findByHashes(any());
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientExists() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);

        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(new ArrayList<>());
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.withRecipient(any(), any())).thenReturn(payload);
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(payload, recipientKey)).thenReturn(new byte[0]);

        final com.quorum.tessera.transaction.ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(senderKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        com.quorum.tessera.transaction.ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadPublisher).publishPayload(any(EncodedPayload.class), eq(senderKey));
        verify(enclave, times(2)).getPublicKeys();
        verify(enclave).unencryptTransaction(payload, recipientKey);
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(payloadEncoder).withRecipient(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void resendAllWhereRequestedIsSenderAndRecipientsListIsNotEmpty() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);

        final EncodedPayload payload = mock(EncodedPayload.class);
        final PublicKey senderKey = PublicKey.from("PUBLICKEY".getBytes());
        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());

        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(Collections.singletonList(recipientKey));
        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .withRecipient(senderKey)
                        .build();

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadPublisher).publishPayload(any(EncodedPayload.class), eq(senderKey));
        verify(enclave).getPublicKeys();
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(payloadEncoder, never()).withRecipient(any(EncodedPayload.class), any(PublicKey.class));
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

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(recipientKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

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

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .withRecipient(recipientKey)
                        .build();

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadEncoder).forRecipient(payload, recipientKey);
        verify(payloadPublisher).publishPayload(any(EncodedPayload.class), eq(recipientKey));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRecipientInKeyList() {

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getRecipientKeys()).thenReturn(singletonList(recipientKey));

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(payload);

        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(recipientKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(payloadEncoder).forRecipient(payload, recipientKey);

        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRequestedIsRecipientThenPublishedPayloadDoesNotContainDataForOtherRecipients() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);

        final PublicKey recipientKey = PublicKey.from("RECIPIENTKEY".getBytes());
        final PublicKey anotherRecipient = PublicKey.from("ANOTHERRECIPIENT".getBytes());
        final List<PublicKey> recipients = List.of(recipientKey, anotherRecipient);

        final RecipientBox recipientBox = RecipientBox.from("box1".getBytes());
        final RecipientBox anotherRecipientBox = RecipientBox.from("box2".getBytes());

        final List<RecipientBox> recipientBoxes = List.of(recipientBox, anotherRecipientBox);

        when(payload.getRecipientKeys()).thenReturn(recipients);
        when(payload.getRecipientBoxes()).thenReturn(recipientBoxes);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        EncodedPayload prunedPayload = mock(EncodedPayload.class);
        when(prunedPayload.getRecipientKeys()).thenReturn(singletonList(recipientKey));
        when(prunedPayload.getRecipientBoxes()).thenReturn(singletonList(recipientBox));
        when(payloadEncoder.forRecipient(payload, recipientKey)).thenReturn(prunedPayload);

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(recipientKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        verify(payloadPublisher).publishPayload(eq(prunedPayload), eq(recipientKey));

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).forRecipient(payload, recipientKey);
        verify(payloadEncoder).decode(encodedData);
        verify(enclave).getPublicKeys();
    }

    @Test
    public void resendAllWhereRequestedIsSenderThenPublishedPayloadIsNotPruned() {

        final byte[] encodedData = "transaction".getBytes();
        final EncryptedTransaction tx = new EncryptedTransaction(mock(MessageHash.class), encodedData);
        final EncodedPayload payload = mock(EncodedPayload.class);

        final RecipientBox recipientBox = RecipientBox.from("box1".getBytes());
        final List<RecipientBox> recipientBoxes = List.of(recipientBox);

        final PublicKey localKey = PublicKey.from("LOCAL_KEY".getBytes());
        final PublicKey senderKey = PublicKey.from("SENDER".getBytes());

        final List<PublicKey> recipients = new ArrayList<>();

        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getSenderKey()).thenReturn(senderKey);
        when(payload.getRecipientKeys()).thenReturn(recipients);
        when(payload.getRecipientBoxes()).thenReturn(recipientBoxes);
        when(payload.getCipherText()).thenReturn("ciphertext".getBytes());

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt())).thenReturn(singletonList(tx));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);
        when(payloadEncoder.withRecipient(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(payload);
        when(enclave.getPublicKeys()).thenReturn(singleton(localKey));

        final com.quorum.tessera.transaction.ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .withRecipient(senderKey)
                        .build();

        com.quorum.tessera.transaction.ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        ArgumentCaptor<EncodedPayload> epAC = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(payloadPublisher).publishPayload(epAC.capture(), eq(senderKey));
        EncodedPayload ep = epAC.getValue();
        verify(payloadEncoder).withRecipient(payload, localKey);
        assertThat(ep.getRecipientBoxes()).hasSize(1).containsExactly(recipientBox);
        assertThat(ep.getSenderKey()).isEqualTo(senderKey);
        assertThat(ep.getCipherText()).containsExactly("ciphertext".getBytes());
        verify(payloadEncoder, never()).forRecipient(any(EncodedPayload.class), any(PublicKey.class));

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(encodedData);
        verify(enclave, times(2)).getPublicKeys();
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

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(senderKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        final Throwable throwable = catchThrowable(() -> transactionManager.resend(resendRequest));

        assertThat(throwable)
                .isInstanceOf(RecipientKeyNotFoundException.class)
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
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(publicKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        when(payloadEncoder.forRecipient(eq(encodedPayload), any(PublicKey.class))).thenReturn(encodedPayload);

        doThrow(new PublishPayloadException("msg")).when(payloadPublisher).publishPayload(encodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(payloadPublisher).publishPayload(encodedPayload, publicKey);
        verify(payloadEncoder).decode(any(byte[].class));
        verify(payloadEncoder).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(enclave).getPublicKeys();
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

        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));
        when(otherEncodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(publicKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        when(payloadEncoder.forRecipient(eq(encodedPayload), any(PublicKey.class))).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(eq(otherEncodedPayload), any(PublicKey.class)))
                .thenReturn(otherEncodedPayload);

        doThrow(new PublishPayloadException("msg")).when(payloadPublisher).publishPayload(encodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(payloadPublisher).publishPayload(encodedPayload, publicKey);
        verify(payloadPublisher).publishPayload(otherEncodedPayload, publicKey);
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(enclave, times(2)).getPublicKeys();
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
        PublicKey publicKey = PublicKey.from(publicKeyBytes);
        when(encodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));
        when(otherEncodedPayload.getRecipientKeys()).thenReturn(Collections.singletonList(publicKey));

        when(payloadEncoder.forRecipient(encodedPayload, publicKey)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(otherEncodedPayload, publicKey)).thenReturn(otherEncodedPayload);

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(publicKey)
                        .withType(ResendRequest.ResendRequestType.ALL)
                        .build();

        doThrow(new PublishPayloadException("msg")).when(payloadPublisher).publishPayload(encodedPayload, publicKey);

        doThrow(new PublishPayloadException("msg"))
                .when(payloadPublisher)
                .publishPayload(otherEncodedPayload, publicKey);

        transactionManager.resend(resendRequest);

        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadPublisher).publishPayload(encodedPayload, publicKey);
        verify(payloadPublisher).publishPayload(otherEncodedPayload, publicKey);
        verify(payloadEncoder, times(2)).decode(any(byte[].class));
        verify(payloadEncoder, times(2)).forRecipient(any(EncodedPayload.class), any(PublicKey.class));
        verify(payloadPublisher, times(2)).publishPayload(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void resendIndividualNoExistingTransactionFound() {

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        String publicKeyData = Base64.getEncoder().encodeToString("PUBLICKEY".getBytes());
        PublicKey recipientKey = PublicKey.from(publicKeyData.getBytes());

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("KEY".getBytes());

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(recipientKey)
                        .withHash(transactionHash)
                        .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
                        .build();

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
        when(encodedPayload.getRecipientBoxes())
                .thenReturn(singletonList(RecipientBox.from("RECIPIENTBOX".getBytes())));

        byte[] encodedOutcome = "SUCCESS".getBytes();
        PublicKey recipientKey = PublicKey.from("PUBLICKEY".getBytes());

        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(payloadEncoder.forRecipient(encodedPayload, recipientKey)).thenReturn(encodedPayload);

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("KEY".getBytes());

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(recipientKey)
                        .withHash(transactionHash)
                        .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
                        .build();

        ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isSameAs(encodedPayload);

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(payloadEncoder).forRecipient(encodedPayload, recipientKey);
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
        when(encodedPayload.getRecipientBoxes())
                .thenReturn(singletonList(RecipientBox.from("RECIPIENTBOX".getBytes())));
        when(encodedPayload.getRecipientKeys()).thenReturn(new ArrayList<>());

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));
        when(payloadEncoder.decode(encodedPayloadData)).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(singleton(recipientKey));
        when(enclave.unencryptTransaction(any(), any())).thenReturn(new byte[0]);

        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn("KEY".getBytes());

        final ResendRequest resendRequest =
                ResendRequest.Builder.create()
                        .withRecipient(senderKey)
                        .withHash(transactionHash)
                        .withType(ResendRequest.ResendRequestType.INDIVIDUAL)
                        .build();

        final ResendResponse result = transactionManager.resend(resendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isNotNull();

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(encodedPayloadData);
        verify(enclave).getPublicKeys();
        verify(enclave).unencryptTransaction(any(), any());
    }

    @Test
    public void receive() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());

        ReceiveRequest receiveRequest = mock(ReceiveRequest.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(keyData);
        when(receiveRequest.getTransactionHash()).thenReturn(messageHash);
        when(receiveRequest.getRecipient()).thenReturn(Optional.of(mock(PublicKey.class)));

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
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
    public void receiveRawTransaction() {
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        PublicKey recipient = PublicKey.from("recipient".getBytes());
        MessageHash messageHash = new MessageHash(Base64.getDecoder().decode(keyData));

        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create()
                        .withRecipient(recipient)
                        .withRaw(true)
                        .withTransactionHash(messageHash)
                        .build();

        final EncryptedRawTransaction encryptedTransaction =
                new EncryptedRawTransaction(
                        messageHash, "payload".getBytes(), "key".getBytes(), "nonce".getBytes(), "sender".getBytes());

        when(encryptedRawTransactionDAO.retrieveByHash(messageHash)).thenReturn(Optional.of(encryptedTransaction));

        when(enclave.unencryptRawPayload(any(RawTransaction.class))).thenReturn("response".getBytes());

        ReceiveResponse response = transactionManager.receive(receiveRequest);

        assertThat(response.getUnencryptedTransactionData()).isEqualTo("response".getBytes());

        verify(enclave).unencryptRawPayload(any(RawTransaction.class));
    }

    @Test
    public void receiveRawTransactionNotFound() {
        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        PublicKey recipient = PublicKey.from("recipient".getBytes());
        MessageHash messageHash = new MessageHash(Base64.getDecoder().decode(keyData));
        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create()
                        .withTransactionHash(messageHash)
                        .withRecipient(recipient)
                        .withRaw(true)
                        .build();

        when(encryptedRawTransactionDAO.retrieveByHash(messageHash)).thenReturn(Optional.empty());

        assertThatExceptionOfType(TransactionNotFoundException.class)
                .isThrownBy(() -> transactionManager.receive(receiveRequest));
    }

    @Test
    public void receiveWithAffectedContractTransactions() {

        byte[] keyData = Base64.getEncoder().encode("KEY".getBytes());
        PublicKey recipient = PublicKey.from("recipient".getBytes());
        MessageHash messageHash = new MessageHash(keyData);

        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create().withRecipient(recipient).withTransactionHash(messageHash).build();

        final EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, keyData);

        final String b64AffectedTxHash =
                "bfMIqWJ/QGQhkK4USxMBxduzfgo/SIGoCros5bWYfPKUBinlAUCqLVOUAP9q+BgLlsWni1M6rnzfmaqSw2J5hQ==";
        final Map<TxHash, SecurityHash> affectedTxs =
                Map.of(new TxHash(b64AffectedTxHash), SecurityHash.from("encoded".getBytes()));

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedTxs);

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
        assertThat(receiveResponse.getExecHash()).isEqualTo("execHash".getBytes());
        assertThat(receiveResponse.getAffectedTransactions()).hasSize(1);

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void receiveNoTransactionInDatabase() {

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
            failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
        } catch (RecipientKeyNotFoundException ex) {
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
            failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
        } catch (RecipientKeyNotFoundException ex) {
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
            failBecauseExceptionWasNotThrown(RecipientKeyNotFoundException.class);
        } catch (RecipientKeyNotFoundException ex) {
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

    @Test(expected = NullPointerException.class)
    public void storeRawWithEmptySender() {
        byte[] sender = "SENDER".getBytes();
        RawTransaction rawTransaction =
                new RawTransaction(
                        "CIPHERTEXT".getBytes(),
                        "SomeKey".getBytes(),
                        new Nonce("nonce".getBytes()),
                        PublicKey.from(sender));
        when(enclave.encryptRawPayload(any(), any())).thenReturn(rawTransaction);
        when(enclave.defaultPublicKey()).thenReturn(PublicKey.from(sender));
        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        StoreRawRequest sendRequest = StoreRawRequest.Builder.create().withPayload(payload).build();

        MessageHash expectedHash = messageHashFactory.createFromCipherText("CIPHERTEXT".getBytes());

        try {
            transactionManager.store(sendRequest);
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (NullPointerException ex) {
            assertThat(ex).hasMessage("Sender is required");
            verify(enclave).encryptRawPayload(eq(payload), eq(PublicKey.from(sender)));
            verify(enclave).defaultPublicKey();

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
    }

    @Test
    public void constructWithLessArgs() {
        final MockServiceLocator serviceLocator = (MockServiceLocator) ServiceLocator.create();

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setApp(AppType.P2P);
        config.setServerConfigs(Arrays.asList(serverConfig));

        serviceLocator.setServices(Stream.of(config, payloadPublisher, enclave).collect(Collectors.toSet()));

        TransactionManager tm =
                new TransactionManagerImpl(
                        Base64Codec.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        payloadPublisher,
                        batchPayloadPublisher,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        privacyHelper,
                        1000);

        assertThat(tm).isNotNull();
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

    //    @Test
    //    public void create() {
    //
    //        Config config = mock(Config.class);
    //        ServerConfig serverConfig = mock(ServerConfig.class);
    //        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
    //        when(config.getP2PServerConfig()).thenReturn(serverConfig);
    //
    //        JdbcConfig jdbcConfig = mock(JdbcConfig.class);
    //        when(jdbcConfig.getUsername()).thenReturn("junit");
    //        when(jdbcConfig.getPassword()).thenReturn("junit");
    //        when(jdbcConfig.getUrl()).thenReturn("jdbc:h2:mem:junit");
    //        when(config.getJdbcConfig()).thenReturn(jdbcConfig);
    //
    //        TransactionManager transactionManager = TransactionManager.create(config);
    //        assertThat(transactionManager).isNotNull();
    //
    //    }

    @Test
    public void defaultPublicKey() {
        transactionManager.defaultPublicKey();
        verify(enclave).defaultPublicKey();
    }
}
