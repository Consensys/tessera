package com.quorum.tessera.transaction;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
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
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.util.Base64Codec;
import com.quorum.tessera.enclave.PayloadDigest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;
import java.util.concurrent.Callable;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private ResendManager resendManager;

    private Enclave enclave;

    private PayloadDigest mockDigest;

    private PrivacyHelper privacyHelper;

    private BatchPayloadPublisher batchPayloadPublisher;

    @Before
    public void onSetUp() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        resendManager = mock(ResendManager.class);
        privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO, true);
        batchPayloadPublisher = mock(BatchPayloadPublisher.class);
        mockDigest = cipherText -> cipherText;

        transactionManager =
                new TransactionManagerImpl(
                        Base64Codec.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        batchPayloadPublisher,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        privacyHelper,
                        mockDigest);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave, resendManager, batchPayloadPublisher);
        verifyNoMoreInteractions(payloadEncoder, encryptedTransactionDAO);
    }

    @Test
    public void send() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any())).thenReturn(encodedPayload);

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        when(enclave.getPublicKeys()).thenReturn(Set.of(receiver));

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendRequest.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash().toString()).isEqualTo("Q0lQSEVSVEVYVA==");
        assertThat(result.getManagedParties()).containsExactly(receiver);

        verify(enclave).encryptPayload(any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(enclave).getPublicKeys();
    }

    @Test
    public void sendAlsoWithPublishCallbackCoverage() {

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any())).thenReturn(encodedPayload);

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
        when(sendRequest.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash().toString()).isEqualTo("Q0lQSEVSVEVYVA==");
        assertThat(result.getManagedParties()).isEmpty();

        verify(enclave).encryptPayload(any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(enclave).getPublicKeys();
        verify(batchPayloadPublisher).publishPayload(any(), anyList());
    }

    @Test
    public void sendWithDuplicateRecipients() {
        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        when(enclave.encryptPayload(any(), any(), any(), any())).thenReturn(encodedPayload);
        when(enclave.getForwardingKeys()).thenReturn(Set.of(PublicKey.from("RECEIVER".getBytes())));

        PublicKey sender = PublicKey.from("SENDER".getBytes());
        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());

        SendRequest sendRequest = mock(SendRequest.class);
        when(sendRequest.getPayload()).thenReturn(payload);
        when(sendRequest.getSender()).thenReturn(sender);
        when(sendRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendRequest.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        SendResponse result = transactionManager.send(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash().toString()).isEqualTo("Q0lQSEVSVEVYVA==");
        assertThat(result.getManagedParties()).isEmpty();

        verify(enclave).encryptPayload(any(), any(), any(), any());
        verify(payloadEncoder).encode(encodedPayload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(enclave).getForwardingKeys();
        verify(enclave).getPublicKeys();
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

        when(payload.getCipherText()).thenReturn("ENCRYPTED_PAYLOAD".getBytes());

        when(enclave.encryptPayload(any(RawTransaction.class), any(), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        when(enclave.getPublicKeys()).thenReturn(Set.of(receiver));

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());
        when(sendSignedRequest.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isEqualTo(new MessageHash("HASH".getBytes()));
        assertThat(result.getManagedParties()).containsExactly(receiver);

        ArgumentCaptor<PrivacyMetadata> data = ArgumentCaptor.forClass(PrivacyMetadata.class);

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), data.capture());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(enclave).getPublicKeys();

        final PrivacyMetadata passingData = data.getValue();
        assertThat(passingData.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(passingData.getPrivacyGroupId()).isNotPresent();
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

        when(enclave.encryptPayload(any(RawTransaction.class), any(), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());
        when(sendSignedRequest.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(sendSignedRequest.getAffectedContractTransactions()).thenReturn(emptySet());
        when(sendSignedRequest.getExecHash()).thenReturn("execHash".getBytes());
        when(sendSignedRequest.getPrivacyGroupId())
                .thenReturn(Optional.of(PrivacyGroup.Id.fromBytes("group".getBytes())));

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isEqualTo(new MessageHash("HASH".getBytes()));
        assertThat(result.getManagedParties()).isEmpty();

        ArgumentCaptor<PrivacyMetadata> data = ArgumentCaptor.forClass(PrivacyMetadata.class);

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), data.capture());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(enclave).getPublicKeys();
        verify(batchPayloadPublisher).publishPayload(any(), anyList());

        final PrivacyMetadata passingData = data.getValue();
        assertThat(passingData.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
        assertThat(passingData.getAffectedContractTransactions()).isEmpty();
        assertThat(passingData.getExecHash()).isEqualTo("execHash".getBytes());
        assertThat(passingData.getPrivacyGroupId())
                .isPresent()
                .get()
                .isEqualTo(PrivacyGroup.Id.fromBytes("group".getBytes()));
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
        when(enclave.encryptPayload(any(RawTransaction.class), any(), any())).thenReturn(payload);

        PublicKey receiver = PublicKey.from("RECEIVER".getBytes());

        SendSignedRequest sendSignedRequest = mock(SendSignedRequest.class);
        when(sendSignedRequest.getRecipients()).thenReturn(List.of(receiver));
        when(sendSignedRequest.getSignedData()).thenReturn("HASH".getBytes());
        when(sendSignedRequest.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        SendResponse result = transactionManager.sendSignedTransaction(sendSignedRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isEqualTo(new MessageHash("HASH".getBytes()));
        assertThat(result.getManagedParties()).isEmpty();

        verify(enclave).encryptPayload(any(RawTransaction.class), any(), any());
        verify(payloadEncoder).encode(payload);
        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class), any(Callable.class));
        verify(encryptedRawTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getForwardingKeys();
        verify(enclave).getPublicKeys();
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
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        transactionManager.storePayload(payload);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
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
        EncodedPayload payload = mock(EncodedPayload.class);

        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

        transactionManager.storePayload(payload);

        verify(encryptedTransactionDAO).save(any(EncryptedTransaction.class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
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

        when(encryptedTransactionDAO.findByHashes(any())).thenReturn(List.of(affectedContractTx));
        when(affectedContractTx.getEncodedPayload()).thenReturn(affectedContractPayload);
        when(payloadEncoder.decode(affectedContractPayload)).thenReturn(affectedContractEncodedPayload);

        transactionManager.storePayload(payload);
        // Ignore transaction - not save
        verify(encryptedTransactionDAO).findByHashes(any());
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
        Map<TxHash, SecurityHash> affectedTx =
                Map.of(TxHash.from("invalidHash".getBytes()), SecurityHash.from("security".getBytes()));

        final EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getSenderKey()).thenReturn(PublicKey.from("sender".getBytes()));
        when(payload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());
        when(payload.getCipherTextNonce()).thenReturn(new Nonce("nonce".getBytes()));
        when(payload.getRecipientBoxes()).thenReturn(List.of(RecipientBox.from("box1".getBytes())));
        when(payload.getRecipientNonce()).thenReturn(new Nonce("recipientNonce".getBytes()));
        when(payload.getRecipientKeys()).thenReturn(singletonList(PublicKey.from("recipient".getBytes())));
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
        when(payload.getAffectedContractTransactions()).thenReturn(affectedTx);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class))).thenReturn(Optional.empty());

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
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(), any());
    }

    @Test
    public void storePayloadWithExistingRecipientAndMismatchedContents() {
        EncryptedTransaction existingDatabaseEntry =
                new EncryptedTransaction(new MessageHash(new byte[0]), new byte[0]);
        EncodedPayload existingPayload = EncodedPayload.Builder.create().withCipherText("ct1".getBytes()).build();
        when(payloadEncoder.decode(any())).thenReturn(existingPayload);
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(existingDatabaseEntry));

        EncodedPayload payloadToStore = EncodedPayload.Builder.create().withCipherText("ct2".getBytes()).build();

        final Throwable throwable = catchThrowable(() -> transactionManager.storePayload(payloadToStore));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("Invalid existing transaction");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(any());
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void storePayloadWithExistingRecipientPSVRecipientNotFound() {
        EncryptedTransaction existingDatabaseEntry =
                new EncryptedTransaction(new MessageHash(new byte[0]), new byte[0]);
        EncodedPayload existingPayload =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withExecHash("execHash".getBytes())
                        .build();
        when(payloadEncoder.decode(any())).thenReturn(existingPayload);
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(existingDatabaseEntry));

        EncodedPayload payloadToStore =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withExecHash("execHash".getBytes())
                        .withRecipientKey(PublicKey.from("recipient1".getBytes()))
                        .withRecipientBox("recipient_box1".getBytes())
                        .build();

        final Throwable throwable = catchThrowable(() -> transactionManager.storePayload(payloadToStore));

        assertThat(throwable).isInstanceOf(RuntimeException.class).hasMessage("expected recipient not found");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(any());
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void storePayloadWithExistingRecipientPSV() {
        privacyHelper = mock(PrivacyHelper.class);
        transactionManager =
                new TransactionManagerImpl(
                        Base64Codec.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        batchPayloadPublisher,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        privacyHelper,
                        mockDigest);

        when(privacyHelper.validatePayload(any(), any(), any())).thenReturn(true);

        PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

        EncryptedTransaction existingDatabaseEntry =
                new EncryptedTransaction(new MessageHash(new byte[0]), new byte[0]);
        EncodedPayload existingPayload =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withAffectedContractTransactions(Map.of(TxHash.from(new byte[0]), new byte[0]))
                        .withExecHash("execHash".getBytes())
                        .withRecipientKeys(List.of(recipient1, recipient2))
                        .build();
        when(payloadEncoder.decode(any())).thenReturn(existingPayload);
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(existingDatabaseEntry));

        EncodedPayload payloadToStore =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
                        .withAffectedContractTransactions(Map.of(TxHash.from(new byte[0]), new byte[0]))
                        .withExecHash("execHash".getBytes())
                        .withRecipientKeys(List.of(recipient1, recipient2))
                        .withRecipientBox("recipient_box1".getBytes())
                        .build();

        MessageHash response = transactionManager.storePayload(payloadToStore);

        assertThat(response.toString()).isEqualTo("Y3Qx");

        ArgumentCaptor<EncodedPayload> payloadCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(payloadEncoder).encode(payloadCaptor.capture());

        EncodedPayload updatedTransaction = payloadCaptor.getValue();
        assertThat(updatedTransaction.getRecipientKeys()).containsExactly(recipient1, recipient2);
        assertThat(updatedTransaction.getRecipientBoxes())
                .containsExactly(RecipientBox.from("recipient_box1".getBytes()));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(encryptedTransactionDAO).update(existingDatabaseEntry);
        verify(payloadEncoder).decode(any());
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void storePayloadWithExistingRecipientNonPSV() {
        PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

        EncryptedTransaction existingDatabaseEntry =
                new EncryptedTransaction(new MessageHash(new byte[0]), new byte[0]);
        EncodedPayload existingPayload =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientKeys(List.of(recipient1))
                        .withRecipientBox("recipient_box1".getBytes())
                        .build();
        when(payloadEncoder.decode(any())).thenReturn(existingPayload);
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(existingDatabaseEntry));

        EncodedPayload payloadToStore =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientKeys(List.of(recipient2))
                        .withRecipientBox("recipient_box2".getBytes())
                        .build();

        MessageHash response = transactionManager.storePayload(payloadToStore);

        assertThat(response.toString()).isEqualTo("Y3Qx");

        ArgumentCaptor<EncodedPayload> payloadCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(payloadEncoder).encode(payloadCaptor.capture());

        EncodedPayload updatedTransaction = payloadCaptor.getValue();
        assertThat(updatedTransaction.getRecipientKeys()).containsExactly(recipient2, recipient1);
        assertThat(updatedTransaction.getRecipientBoxes())
                .containsExactly(
                        RecipientBox.from("recipient_box2".getBytes()), RecipientBox.from("recipient_box1".getBytes()));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(encryptedTransactionDAO).update(existingDatabaseEntry);
        verify(payloadEncoder).decode(any());
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void storePayloadWithDuplicateExistingRecipient() {
        PublicKey recipient1 = PublicKey.from("recipient1".getBytes());

        EncryptedTransaction existingDatabaseEntry =
                new EncryptedTransaction(new MessageHash(new byte[0]), new byte[0]);

        EncodedPayload existingPayload =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientKeys(List.of(recipient1))
                        .withRecipientBox("recipient_box1".getBytes())
                        .build();

        when(payloadEncoder.decode(any())).thenReturn(existingPayload);
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(existingDatabaseEntry));

        EncodedPayload payloadToStore =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientKeys(List.of(recipient1))
                        .withRecipientBox("recipient_box1".getBytes())
                        .build();

        MessageHash response = transactionManager.storePayload(payloadToStore);

        assertThat(response.toString()).isEqualTo("Y3Qx");

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(payloadEncoder).decode(any());
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void storePayloadWithExistingRecipientLegacyNoRecipients() {
        EncryptedTransaction existingDatabaseEntry =
                new EncryptedTransaction(new MessageHash(new byte[0]), new byte[0]);
        EncodedPayload existingPayload =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientBox("recipient_box1".getBytes())
                        .build();
        when(payloadEncoder.decode(any())).thenReturn(existingPayload);
        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(existingDatabaseEntry));

        EncodedPayload payloadToStore =
                EncodedPayload.Builder.create()
                        .withCipherText("ct1".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withRecipientBox("recipient_box2".getBytes())
                        .build();

        MessageHash response = transactionManager.storePayload(payloadToStore);

        assertThat(response.toString()).isEqualTo("Y3Qx");

        ArgumentCaptor<EncodedPayload> payloadCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(payloadEncoder).encode(payloadCaptor.capture());

        EncodedPayload updatedTransaction = payloadCaptor.getValue();
        assertThat(updatedTransaction.getRecipientKeys()).isEmpty();
        assertThat(updatedTransaction.getRecipientBoxes())
                .containsExactly(
                        RecipientBox.from("recipient_box2".getBytes()), RecipientBox.from("recipient_box1".getBytes()));

        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(encryptedTransactionDAO).update(existingDatabaseEntry);
        verify(payloadEncoder).decode(any());
        verify(enclave).getPublicKeys();
        verify(enclave).findInvalidSecurityHashes(any(EncodedPayload.class), anyList());
    }

    @Test
    public void receive() {
        PublicKey sender = PublicKey.from("sender".getBytes());
        byte[] randomData = Base64.getEncoder().encode("odd-data".getBytes());
        MessageHash messageHash = new MessageHash(randomData);

        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create()
                        .withRecipient(mock(PublicKey.class))
                        .withTransactionHash(messageHash)
                        .build();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, randomData);

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getSenderKey()).thenReturn(sender);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();
        assertThat(receiveResponse.sender()).isEqualTo(sender);
        assertThat(receiveResponse.getUnencryptedTransactionData()).isEqualTo(expectedOutcome);
        assertThat(receiveResponse.getPrivacyGroupId()).isNotPresent();

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void receiveWithPrivacyGroupId() {
        PublicKey sender = PublicKey.from("sender".getBytes());
        byte[] randomData = Base64.getEncoder().encode("odd-data".getBytes());
        MessageHash messageHash = new MessageHash(randomData);

        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create()
                        .withRecipient(mock(PublicKey.class))
                        .withTransactionHash(messageHash)
                        .build();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, randomData);

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(payload.getSenderKey()).thenReturn(sender);
        when(payload.getPrivacyGroupId()).thenReturn(Optional.of(PrivacyGroup.Id.fromBytes("group".getBytes())));
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();
        assertThat(receiveResponse.sender()).isEqualTo(sender);
        assertThat(receiveResponse.getUnencryptedTransactionData()).isEqualTo(expectedOutcome);
        assertThat(receiveResponse.getPrivacyGroupId()).isPresent();
        assertThat(receiveResponse.getPrivacyGroupId().get()).isEqualTo(PrivacyGroup.Id.fromBytes("group".getBytes()));

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).getPublicKeys();
    }

    @Test
    public void receiveWithRecipientsPresent() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());
        final PublicKey recipient2 = PublicKey.from("recipient2".getBytes());

        byte[] randomData = Base64.getEncoder().encode("odd-data".getBytes());
        MessageHash messageHash = new MessageHash(randomData);

        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create()
                        .withRecipient(mock(PublicKey.class))
                        .withTransactionHash(messageHash)
                        .build();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, randomData);

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getExecHash()).thenReturn("execHash".getBytes());
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getRecipientKeys()).thenReturn(List.of(recipient1, recipient2));
        when(payload.getSenderKey()).thenReturn(sender);
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);
        when(enclave.getPublicKeys()).thenReturn(Set.of(recipient1, recipient2));

        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();
        assertThat(receiveResponse.getUnencryptedTransactionData()).isEqualTo(expectedOutcome);
        assertThat(receiveResponse.getManagedParties()).containsExactlyInAnyOrder(recipient1, recipient2);
        assertThat(receiveResponse.sender()).isEqualTo(sender);

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(2)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave, times(2)).getPublicKeys();
    }

    @Test
    public void receiveWithNoRecipientsPresent() {
        final PublicKey sender = PublicKey.from("sender".getBytes());
        final PublicKey recipient1 = PublicKey.from("recipient1".getBytes());

        byte[] randomData = Base64.getEncoder().encode("odd-data".getBytes());
        MessageHash messageHash = new MessageHash(randomData);

        ReceiveRequest receiveRequest =
                ReceiveRequest.Builder.create()
                        .withRecipient(mock(PublicKey.class))
                        .withTransactionHash(messageHash)
                        .build();

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction(messageHash, randomData);

        EncodedPayload payload = mock(EncodedPayload.class);
        when(payload.getSenderKey()).thenReturn(sender);
        when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(payload.getRecipientBoxes()).thenReturn(List.of(RecipientBox.from("box1".getBytes())));
        when(payloadEncoder.decode(any(byte[].class))).thenReturn(payload);

        when(encryptedTransactionDAO.retrieveByHash(any(MessageHash.class)))
                .thenReturn(Optional.of(encryptedTransaction));

        byte[] expectedOutcome = "Encrypted payload".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(expectedOutcome);
        when(enclave.getPublicKeys()).thenReturn(Set.of(recipient1));

        ReceiveResponse receiveResponse = transactionManager.receive(receiveRequest);

        assertThat(receiveResponse).isNotNull();
        assertThat(receiveResponse.getUnencryptedTransactionData()).isEqualTo(expectedOutcome);
        assertThat(receiveResponse.getManagedParties()).containsExactly(recipient1);
        assertThat(receiveResponse.sender()).isEqualTo(sender);

        verify(payloadEncoder).decode(any(byte[].class));
        verify(encryptedTransactionDAO).retrieveByHash(any(MessageHash.class));
        verify(enclave, times(3)).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave, times(2)).getPublicKeys();
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
        PublicKey sender = PublicKey.from("sender".getBytes());
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
        when(payload.getSenderKey()).thenReturn(sender);

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
        assertThat(receiveResponse.sender()).isEqualTo(sender);

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

        MessageHash expectedHash = new MessageHash(mockDigest.digest("CIPHERTEXT".getBytes()));

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

        MessageHash expectedHash = new MessageHash(mockDigest.digest("CIPHERTEXT".getBytes()));

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
        config.setServerConfigs(List.of(serverConfig));

        serviceLocator.setServices(Set.of(config, enclave));

        TransactionManager tm =
                new TransactionManagerImpl(
                        Base64Codec.create(),
                        payloadEncoder,
                        encryptedTransactionDAO,
                        batchPayloadPublisher,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        privacyHelper,
                        mockDigest);

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

    @Test
    public void defaultPublicKey() {
        transactionManager.defaultPublicKey();
        verify(enclave).defaultPublicKey();
    }

    @Test
    public void upcheckReturnsTrue() {

        when(encryptedTransactionDAO.upcheck()).thenReturn(true);
        when(encryptedRawTransactionDAO.upcheck()).thenReturn(true);

        assertThat(transactionManager.upcheck()).isTrue();

        verify(encryptedRawTransactionDAO).upcheck();
        verify(encryptedTransactionDAO).upcheck();
    }

    @Test
    public void upcheckReturnsFalseIfEncryptedTransactionDBFail() {

        when(encryptedTransactionDAO.upcheck()).thenReturn(false);
        when(encryptedRawTransactionDAO.upcheck()).thenReturn(true);

        assertThat(transactionManager.upcheck()).isFalse();

        verify(encryptedRawTransactionDAO).upcheck();
        verify(encryptedTransactionDAO).upcheck();
    }

    @Test
    public void upcheckReturnsFalseIfEncryptedRawTransactionDBFail() {

        when(encryptedTransactionDAO.upcheck()).thenReturn(true);
        when(encryptedRawTransactionDAO.upcheck()).thenReturn(false);

        assertThat(transactionManager.upcheck()).isFalse();

        verify(encryptedRawTransactionDAO).upcheck();
    }
}
