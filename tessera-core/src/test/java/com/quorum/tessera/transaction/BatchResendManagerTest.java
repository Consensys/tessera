package com.quorum.tessera.transaction;

import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.data.*;
import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.service.Service;
import com.quorum.tessera.transaction.exception.KeyNotFoundException;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import com.quorum.tessera.util.Base64Decoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BatchResendManagerTest {

    private PayloadEncoder payloadEncoder;

    private Enclave enclave;

    private TransactionManagerWrapper transactionManager;

    private StagingEntityDAO stagingEntityDAO;

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private ResendBatchPublisher publisher;

    private BatchResendManager manager;

    private static final String KEY_STRING = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";

    private final PublicKey publicKey = PublicKey.from(Base64Decoder.create().decode(KEY_STRING));

    @Before
    public void init() {
        payloadEncoder = mock(PayloadEncoder.class);
        enclave = mock(Enclave.class);
        transactionManager = mock(TransactionManagerWrapper.class);
        stagingEntityDAO = mock(StagingEntityDAO.class);
        encryptedTransactionDAO = mock(EncryptedTransactionDAO.class);
        publisher = mock(ResendBatchPublisher.class);

        manager =
                new BatchResendManagerImpl(
                        payloadEncoder,
                        Base64Decoder.create(),
                        enclave,
                        transactionManager,
                        stagingEntityDAO,
                        encryptedTransactionDAO,
                        publisher);

        when(enclave.status()).thenReturn(Service.Status.STARTED);

        final PublicKey publicKey =
                PublicKey.from(Base64Decoder.create().decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(payloadEncoder);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(transactionManager);
        verifyNoMoreInteractions(stagingEntityDAO);
        verifyNoMoreInteractions(encryptedTransactionDAO);
        verifyNoMoreInteractions(publisher);
    }

    @Test
    public void testResendBatchWhenRequestedNodeIsSender() {

        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(2);
        request.setPublicKey(KEY_STRING);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(new MessageHash("hash".getBytes()));
        encryptedTransaction.setEncodedPayload("encodedPayload".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt()))
                .thenReturn(singletonList(encryptedTransaction));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode("encodedPayload".getBytes())).thenReturn(encodedPayload);

        when(encodedPayload.getSenderKey()).thenReturn(publicKey);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        Mockito.doNothing().when(publisher).publishBatch(any(), any(PublicKey.class));

        ResendBatchResponse response = manager.resendBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(1);

        verify(enclave).getPublicKeys();
        verify(enclave).status();
        verify(enclave).unencryptTransaction(any(), any());
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).withRecipient(same(encodedPayload), any());
        verify(payloadEncoder).decode(any());
        verify(publisher).publishBatch(any(), any(PublicKey.class));
    }

    @Test
    public void testResendBatchWhenRequestedNodeIsSender2() {

        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(2);
        request.setPublicKey(KEY_STRING);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(new MessageHash("hash".getBytes()));
        encryptedTransaction.setEncodedPayload("encodedPayload".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt()))
                .thenReturn(singletonList(encryptedTransaction));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode("encodedPayload".getBytes())).thenReturn(encodedPayload);

        when(encodedPayload.getSenderKey()).thenReturn(publicKey);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.PARTY_PROTECTION);
        List<PublicKey> recipientKeys = new ArrayList<>();
        recipientKeys.add(publicKey);
        when(encodedPayload.getRecipientKeys()).thenReturn(recipientKeys);

        Mockito.doNothing().when(publisher).publishBatch(any(), any(PublicKey.class));

        ResendBatchResponse response = manager.resendBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(1);

        verify(enclave).getPublicKeys();
        verify(enclave).status();
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(any());
        verify(publisher).publishBatch(any(), any(PublicKey.class));
    }

    @Test
    public void testResendBatchWhenRequestedNodeIsSenderAndNoKeyFoundToDecrypt() {
        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(2);
        request.setPublicKey(KEY_STRING);

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(new MessageHash("hash".getBytes()));
        encryptedTransaction.setEncodedPayload("encodedPayload".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt()))
                .thenReturn(singletonList(encryptedTransaction));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode("encodedPayload".getBytes())).thenReturn(encodedPayload);

        when(enclave.unencryptTransaction(any(), any())).thenThrow(NaclException.class);

        when(encodedPayload.getSenderKey()).thenReturn(publicKey);
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(encodedPayload.getCipherText()).thenReturn("CIPHERTEXT".getBytes());

        Mockito.doNothing().when(publisher).publishBatch(any(), any(PublicKey.class));

        assertThatExceptionOfType(KeyNotFoundException.class)
                .isThrownBy(
                        () -> {
                            manager.resendBatch(request);
                            failBecauseExceptionWasNotThrown(any());
                        })
                .withMessage("No key found as recipient of message Q0lQSEVSVEVYVA==");

        verify(enclave).getPublicKeys();
        verify(enclave).status();
        verify(enclave).unencryptTransaction(any(), any());
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO).transactionCount();
        verify(payloadEncoder).decode(any());
    }

    @Test
    public void testResendBatchWhenRequestedNodeIsRecipient() {
        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(2);
        request.setPublicKey(KEY_STRING);

        final PublicKey ownKey = PublicKey.from("ownKey".getBytes());

        EncryptedTransaction encryptedTransaction = new EncryptedTransaction();
        encryptedTransaction.setHash(new MessageHash("hash".getBytes()));
        encryptedTransaction.setEncodedPayload("encodedPayload".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt()))
                .thenReturn(singletonList(encryptedTransaction));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode("encodedPayload".getBytes())).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(ownKey));
        when(encodedPayload.getSenderKey()).thenReturn(ownKey);
        when(encodedPayload.getRecipientKeys()).thenReturn(singletonList(publicKey));
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        Mockito.doNothing().when(publisher).publishBatch(any(), any(PublicKey.class));

        ResendBatchResponse response = manager.resendBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(1);

        verify(enclave).getPublicKeys();
        verify(enclave).status();
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(any());
        verify(payloadEncoder).forRecipient(encodedPayload, publicKey);
        verify(publisher).publishBatch(any(), any(PublicKey.class));
    }

    @Test
    public void testResendBatchSizeReached() {

        final int batchSize = 1;

        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(batchSize);
        request.setPublicKey(KEY_STRING);

        final PublicKey ownKey = PublicKey.from("ownKey".getBytes());

        EncryptedTransaction encryptedTransaction1 = new EncryptedTransaction();
        encryptedTransaction1.setHash(new MessageHash("hash".getBytes()));
        encryptedTransaction1.setEncodedPayload("encodedPayload".getBytes());

        EncryptedTransaction encryptedTransaction2 = new EncryptedTransaction();
        encryptedTransaction2.setHash(new MessageHash("hash2".getBytes()));
        encryptedTransaction2.setEncodedPayload("encodedPayload2".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt()))
                .thenReturn(Arrays.asList(encryptedTransaction1, encryptedTransaction2));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(2L);

        when(payloadEncoder.decode("encodedPayload".getBytes())).thenReturn(encodedPayload);
        when(payloadEncoder.decode("encodedPayload2".getBytes())).thenReturn(encodedPayload);
        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(ownKey));
        when(encodedPayload.getSenderKey()).thenReturn(ownKey);
        when(encodedPayload.getRecipientKeys()).thenReturn(singletonList(publicKey));
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        doAnswer(
                        invocationOnMock -> {
                            assertThat((ArrayList<EncodedPayload>) invocationOnMock.getArguments()[0])
                                    .size()
                                    .isEqualTo(batchSize);
                            return null;
                        })
                .when(publisher)
                .publishBatch(any(), any(PublicKey.class));

        ResendBatchResponse response = manager.resendBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(2);

        verify(enclave, times(2)).getPublicKeys();
        verify(enclave).status();
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder, times(2)).decode(any());
        verify(payloadEncoder, times(2)).forRecipient(encodedPayload, publicKey);

        verify(publisher, times(2)).publishBatch(any(), any(PublicKey.class));
    }

    @Test
    public void testResendBatchNoneFound() {
        ResendBatchRequest request = new ResendBatchRequest();
        request.setBatchSize(1);
        request.setPublicKey(KEY_STRING);

        final PublicKey ownKey = PublicKey.from("ownKey".getBytes());

        final EncodedPayload encodedPayload = mock(EncodedPayload.class);

        EncryptedTransaction encryptedTransaction1 = new EncryptedTransaction();
        encryptedTransaction1.setHash(new MessageHash("hash".getBytes()));
        encryptedTransaction1.setEncodedPayload("encodedPayload".getBytes());

        when(encryptedTransactionDAO.retrieveTransactions(anyInt(), anyInt()))
                .thenReturn(singletonList(encryptedTransaction1));
        when(encryptedTransactionDAO.transactionCount()).thenReturn(1L);

        when(payloadEncoder.decode("encodedPayload".getBytes())).thenReturn(encodedPayload);

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(ownKey));
        PublicKey someRandomKey = PublicKey.from("someKey".getBytes());
        when(encodedPayload.getSenderKey()).thenReturn(someRandomKey);
        when(encodedPayload.getRecipientKeys()).thenReturn(singletonList(someRandomKey));
        when(encodedPayload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        Mockito.doNothing().when(publisher).publishBatch(any(), any(PublicKey.class));

        ResendBatchResponse response = manager.resendBatch(request);

        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(0);

        verify(enclave).status();
        verify(encryptedTransactionDAO).retrieveTransactions(anyInt(), anyInt());
        verify(encryptedTransactionDAO, times(2)).transactionCount();
        verify(payloadEncoder).decode(any());
    }

    @Test
    public void testResendBatchEnclaveNotAvailable() {
        when(enclave.status()).thenReturn(Service.Status.STOPPED);
        ResendBatchRequest request = mock(ResendBatchRequest.class);

        assertThatExceptionOfType(EnclaveNotAvailableException.class)
                .isThrownBy(
                        () -> {
                            manager.resendBatch(request);
                            failBecauseExceptionWasNotThrown(any());
                        });

        verify(enclave).status();
    }

    @Test
    public void testStoreResendBatch() {
        final EncodedPayload encodedPayload =
                new EncodedPayload(
                        publicKey,
                        "cipherText".getBytes(),
                        new Nonce("nonce".getBytes()),
                        singletonList("box".getBytes()),
                        new Nonce("recipientNonce".getBytes()),
                        singletonList(PublicKey.from("receiverKey".getBytes())),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        final byte[] raw = new PayloadEncoderImpl().encode(encodedPayload);

        PushBatchRequest request = new PushBatchRequest();
        request.setEncodedPayloads(singletonList(raw));

        when(stagingEntityDAO.retrieveByHash(any())).thenReturn(Optional.empty());
        when(stagingEntityDAO.save(any(StagingTransaction.class))).thenReturn(new StagingTransaction());

        manager.storeResendBatch(request);

        verify(stagingEntityDAO).retrieveByHash(any());
        verify(stagingEntityDAO).save(any(StagingTransaction.class));
    }

    @Test
    public void testStoreResendBatchMultipleVersions() {
        final EncodedPayload encodedPayload =
                new EncodedPayload(
                        publicKey,
                        "cipherText".getBytes(),
                        new Nonce("nonce".getBytes()),
                        singletonList("box".getBytes()),
                        new Nonce("recipientNonce".getBytes()),
                        singletonList(PublicKey.from("receiverKey".getBytes())),
                        PrivacyMode.STANDARD_PRIVATE,
                        emptyMap(),
                        new byte[0]);

        final byte[] raw = new PayloadEncoderImpl().encode(encodedPayload);

        PushBatchRequest request = new PushBatchRequest();
        request.setEncodedPayloads(singletonList(raw));

        StagingTransaction existing = new StagingTransaction();

        when(stagingEntityDAO.retrieveByHash(any())).thenReturn(Optional.of(existing));
        when(stagingEntityDAO.update(any(StagingTransaction.class))).thenReturn(new StagingTransaction());

        manager.storeResendBatch(request);

        verify(stagingEntityDAO).retrieveByHash(any());
        verify(stagingEntityDAO).update(any(StagingTransaction.class));
    }

    @Test
    public void testPerformStagingSuccess() {

        StagingTransaction st1 = mock(StagingTransaction.class);
        StagingTransaction st2 = mock(StagingTransaction.class);
        when(st1.getValidationStage()).thenReturn(1L);
        when(st2.getValidationStage()).thenReturn(2L);

        when(stagingEntityDAO.countAll()).thenReturn(2L);
        when(stagingEntityDAO.countStaged()).thenReturn(2L);

        BatchResendManager.Result result = manager.performStaging();

        assertThat(result).isEqualTo(BatchResendManager.Result.SUCCESS);

        verify(stagingEntityDAO).performStaging(anyInt());
        verify(stagingEntityDAO).countAll();
        verify(stagingEntityDAO).countStaged();
    }

    @Test
    public void testPerformStagingPartialSuccess() {

        StagingTransaction st1 = mock(StagingTransaction.class);
        StagingTransaction st2 = mock(StagingTransaction.class);
        when(st1.getValidationStage()).thenReturn(1L);
        when(st2.getValidationStage()).thenReturn(null);

        when(stagingEntityDAO.countAll()).thenReturn(2L);
        when(stagingEntityDAO.countStaged()).thenReturn(1L);

        BatchResendManager.Result result = manager.performStaging();

        assertThat(result).isEqualTo(BatchResendManager.Result.PARTIAL_SUCCESS);

        verify(stagingEntityDAO).performStaging(anyInt());
        verify(stagingEntityDAO).countAll();
        verify(stagingEntityDAO).countStaged();
    }

    @Test
    public void testPerformStagingFailed() {

        StagingTransaction st1 = mock(StagingTransaction.class);
        StagingTransaction st2 = mock(StagingTransaction.class);
        when(st1.getValidationStage()).thenReturn(null);
        when(st2.getValidationStage()).thenReturn(null);

        when(stagingEntityDAO.countAll()).thenReturn(2L);
        when(stagingEntityDAO.countStaged()).thenReturn(0L);

        BatchResendManager.Result result = manager.performStaging();

        assertThat(result).isEqualTo(BatchResendManager.Result.FAILURE);

        verify(stagingEntityDAO).performStaging(anyInt());
        verify(stagingEntityDAO).countAll();
        verify(stagingEntityDAO).countStaged();
    }

    @Test
    public void testPerformSync() {

        StagingTransactionVersion version1 = mock(StagingTransactionVersion.class);
        StagingTransactionVersion version2 = mock(StagingTransactionVersion.class);
        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());
        Map<StagingRecipient, StagingTransactionVersion> versions = new HashMap<>();
        versions.put(mock(StagingRecipient.class), version1);
        versions.put(mock(StagingRecipient.class), version2);

        StagingTransaction stagingTransaction = mock(StagingTransaction.class);

        when(stagingTransaction.getVersions()).thenReturn(versions);

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(singletonList(stagingTransaction));
        when(stagingEntityDAO.countAll()).thenReturn(1L);

        when(transactionManager.storePayloadBypass(any())).thenReturn(new MessageHash("hash".getBytes()));

        manager.performSync();

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(transactionManager).storePayloadBypass("payload1".getBytes());
        verify(transactionManager).storePayloadBypass("payload2".getBytes());
    }

    @Test
    public void testPerformSyncPartiallyFailed() {

        StagingTransactionVersion version1 = mock(StagingTransactionVersion.class);
        StagingTransactionVersion version2 = mock(StagingTransactionVersion.class);
        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());
        Map<StagingRecipient, StagingTransactionVersion> versions = new HashMap<>();
        versions.put(mock(StagingRecipient.class), version1);
        versions.put(mock(StagingRecipient.class), version2);

        StagingTransaction stagingTransaction = mock(StagingTransaction.class);

        when(stagingTransaction.getVersions()).thenReturn(versions);

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(singletonList(stagingTransaction));
        when(stagingEntityDAO.countAll()).thenReturn(1L);

        when(transactionManager.storePayloadBypass("payload1".getBytes())).thenThrow(PrivacyViolationException.class);

        BatchResendManager.Result result = manager.performSync();

        assertThat(result).isEqualTo(BatchResendManager.Result.PARTIAL_SUCCESS);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(transactionManager).storePayloadBypass("payload1".getBytes());
        verify(transactionManager).storePayloadBypass("payload2".getBytes());
    }

    @Test
    public void testPerformSyncFailed() {

        StagingTransactionVersion version1 = mock(StagingTransactionVersion.class);
        StagingTransactionVersion version2 = mock(StagingTransactionVersion.class);
        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());
        Map<StagingRecipient, StagingTransactionVersion> versions = new HashMap<>();
        versions.put(mock(StagingRecipient.class), version1);
        versions.put(mock(StagingRecipient.class), version2);

        StagingTransaction stagingTransaction = mock(StagingTransaction.class);

        when(stagingTransaction.getVersions()).thenReturn(versions);

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(singletonList(stagingTransaction));
        when(stagingEntityDAO.countAll()).thenReturn(1L);

        when(transactionManager.storePayloadBypass(any())).thenThrow(PrivacyViolationException.class);

        BatchResendManager.Result result = manager.performSync();

        assertThat(result).isEqualTo(BatchResendManager.Result.FAILURE);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(transactionManager).storePayloadBypass("payload1".getBytes());
        verify(transactionManager).storePayloadBypass("payload2".getBytes());
    }

    @Test
    public void testPerformSyncIgnoreDataWithConsistencyIssue() {

        StagingTransactionVersion version1 = mock(StagingTransactionVersion.class);
        StagingTransactionVersion version2 = mock(StagingTransactionVersion.class);
        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());
        Map<StagingRecipient, StagingTransactionVersion> versions = new HashMap<>();
        versions.put(mock(StagingRecipient.class), version1);
        versions.put(mock(StagingRecipient.class), version2);

        StagingTransaction stagingTransaction = mock(StagingTransaction.class);

        when(stagingTransaction.getIssues()).thenReturn("Some issues");

        when(stagingTransaction.getVersions()).thenReturn(versions);

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(singletonList(stagingTransaction));
        when(stagingEntityDAO.countAll()).thenReturn(1L);

        when(transactionManager.storePayloadBypass(any())).thenReturn(new MessageHash("hash".getBytes()));

        manager.performSync();

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();
    }

    @Test
    public void testCleanUpStaging() {

        doNothing().when(stagingEntityDAO).cleanStagingArea(anyInt());

        manager.cleanupStagingArea();

        verify(stagingEntityDAO).cleanStagingArea(anyInt());
    }

    @Test
    public void testIsResendMode() {
        assertThat(manager.isResendMode()).isTrue();
    }

    @Test
    public void createWithMinimalConstrcutor() {
        assertThat(
                        new BatchResendManagerImpl(
                                enclave, transactionManager, stagingEntityDAO, encryptedTransactionDAO, publisher))
                .isNotNull();
    }
}
