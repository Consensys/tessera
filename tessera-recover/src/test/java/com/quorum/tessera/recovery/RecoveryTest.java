package com.quorum.tessera.recovery;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecoveryTest extends RecoveryTestCase {

    private Recovery recovery;

    private StagingEntityDAO stagingEntityDAO;

    private BatchTransactionRequester transactionRequester;

    private Discovery discovery;

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    @Before
    public void onSetUp() {

        discovery = mock(Discovery.class);
        when(discovery.getCurrent()).thenReturn(getCurrent());

        transactionRequester = mock(BatchTransactionRequester.class);
        when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(true);

        stagingEntityDAO = mock(StagingEntityDAO.class);
        transactionManager = mock(TransactionManager.class);

        payloadEncoder = mock(PayloadEncoder.class);

        this.recovery =
                new RecoveryImpl(stagingEntityDAO, discovery, transactionRequester, transactionManager, payloadEncoder);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionRequester);
        verifyNoMoreInteractions(discovery);
        verifyNoMoreInteractions(stagingEntityDAO);
        verifyNoMoreInteractions(transactionManager);
        verifyNoMoreInteractions(payloadEncoder);
    }

    @Test
    public void testRequestSuccess() {

        final RecoveryResult result = recovery.request();

        assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

        verify(transactionRequester, times(4)).requestAllTransactionsFromNode(anyString());
        verify(discovery).getCurrent();
    }

    @Test
    public void testRequestPartialSuccess() {

        when(transactionRequester.requestAllTransactionsFromNode(eq("http://party2"))).thenReturn(false);

        final RecoveryResult result = recovery.request();

        assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

        verify(transactionRequester, times(4)).requestAllTransactionsFromNode(anyString());
        verify(discovery).getCurrent();
    }

    @Test
    public void testRequestFailed() {

        when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(false);

        final RecoveryResult result = recovery.request();

        assertThat(result).isEqualTo(RecoveryResult.FAILURE);

        verify(transactionRequester, times(4)).requestAllTransactionsFromNode(anyString());
        verify(discovery).getCurrent();
    }

    @Test
    public void testStagingSuccess() {

        // Staging loop run 3 times until there is no record left
        when(stagingEntityDAO.updateStageForBatch(anyInt(), eq(1L))).thenReturn(1);
        when(stagingEntityDAO.updateStageForBatch(anyInt(), eq(2L))).thenReturn(1);
        when(stagingEntityDAO.updateStageForBatch(anyInt(), eq(3L))).thenReturn(0);

        when(stagingEntityDAO.countAll()).thenReturn(2L);
        when(stagingEntityDAO.countStaged()).thenReturn(2L);

        RecoveryResult result = recovery.stage();

        assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

        verify(stagingEntityDAO, times(3)).updateStageForBatch(anyInt(), anyLong());
        verify(stagingEntityDAO).countAll();
        verify(stagingEntityDAO).countStaged();
    }

    @Test
    public void testStagingPartialSuccess() {

        when(stagingEntityDAO.countAll()).thenReturn(2L);
        when(stagingEntityDAO.countStaged()).thenReturn(1L);

        when(stagingEntityDAO.updateStageForBatch(anyInt(), anyLong())).thenReturn(0);
        RecoveryResult result = recovery.stage();

        assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

        verify(stagingEntityDAO).updateStageForBatch(anyInt(), anyLong());
        verify(stagingEntityDAO).countAll();
        verify(stagingEntityDAO).countStaged();
    }

    @Test
    public void testStagingFailed() {

        when(stagingEntityDAO.updateStageForBatch(anyInt(), anyLong())).thenReturn(0);

        when(stagingEntityDAO.countAll()).thenReturn(2L);
        when(stagingEntityDAO.countStaged()).thenReturn(0L);

        RecoveryResult result = recovery.stage();

        assertThat(result).isEqualTo(RecoveryResult.FAILURE);

        verify(stagingEntityDAO).updateStageForBatch(anyInt(), anyLong());
        verify(stagingEntityDAO).countAll();
        verify(stagingEntityDAO).countStaged();
    }

    @Test
    public void testSyncSuccess() {

        StagingTransaction version1 = mock(StagingTransaction.class);
        StagingTransaction version2 = mock(StagingTransaction.class);

        when(version1.getHash()).thenReturn("TXN1");
        when(version2.getHash()).thenReturn("TXN1");

        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(List.of(version1, version2));
        when(stagingEntityDAO.countAll()).thenReturn(2L);

        when(transactionManager.storePayload(any())).thenReturn(new MessageHash("hash".getBytes()));

        EncodedPayload firstPayload = mock(EncodedPayload.class);
        EncodedPayload secondPayload = mock(EncodedPayload.class);
        when(payloadEncoder.decode("payload1".getBytes())).thenReturn(firstPayload);
        when(payloadEncoder.decode("payload2".getBytes())).thenReturn(secondPayload);

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(payloadEncoder).decode("payload1".getBytes());
        verify(payloadEncoder).decode("payload2".getBytes());

        verify(transactionManager).storePayload(firstPayload);
        verify(transactionManager).storePayload(secondPayload);
    }

    @Test
    public void testSyncPartialSuccess() {

        StagingTransaction version1 = mock(StagingTransaction.class);
        StagingTransaction version2 = mock(StagingTransaction.class);

        when(version1.getHash()).thenReturn("TXN1");
        when(version2.getHash()).thenReturn("TXN1");

        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(List.of(version1, version2));
        when(stagingEntityDAO.countAll()).thenReturn(2L);

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload encodedPayload2 = mock(EncodedPayload.class);
        when(payloadEncoder.decode("payload1".getBytes())).thenReturn(encodedPayload);
        when(payloadEncoder.decode("payload2".getBytes())).thenReturn(encodedPayload2);

        when(transactionManager.storePayload(encodedPayload)).thenThrow(PrivacyViolationException.class);

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(payloadEncoder).decode("payload1".getBytes());
        verify(payloadEncoder).decode("payload2".getBytes());

        verify(transactionManager).storePayload(encodedPayload);
        verify(transactionManager).storePayload(encodedPayload2);
    }

    @Test
    public void testSyncFailed() {

        StagingTransaction version1 = mock(StagingTransaction.class);
        StagingTransaction version2 = mock(StagingTransaction.class);
        when(version1.getHash()).thenReturn("TXN1");
        when(version2.getHash()).thenReturn("TXN1");

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload encodedPayload2 = mock(EncodedPayload.class);

        when(payloadEncoder.decode(any())).thenReturn(encodedPayload).thenReturn(encodedPayload2);

        List<StagingTransaction> stagingTransactions = List.of(version1, version2);

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(stagingTransactions);

        when(stagingEntityDAO.countAll()).thenReturn((long) stagingTransactions.size());

        when(transactionManager.storePayload(any())).thenThrow(PrivacyViolationException.class);

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.FAILURE);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(payloadEncoder, times(2)).decode(any());

        verify(transactionManager, times(2)).storePayload(any());
    }

    @Test
    public void testSyncPsvTransactionOnlySentOnce() {

        StagingTransaction version1 = mock(StagingTransaction.class);
        StagingTransaction version2 = mock(StagingTransaction.class);
        StagingTransaction anotherTx = mock(StagingTransaction.class);

        when(version1.getHash()).thenReturn("TXN1");
        when(version2.getHash()).thenReturn("TXN1");
        when(anotherTx.getHash()).thenReturn("TXN2");

        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload1".getBytes());
        when(anotherTx.getPayload()).thenReturn("payload2".getBytes());

        EncodedPayload encodedPayload = mock(EncodedPayload.class);
        EncodedPayload encodedPayload2 = mock(EncodedPayload.class);
        when(payloadEncoder.decode("payload1".getBytes())).thenReturn(encodedPayload);
        when(payloadEncoder.decode("payload2".getBytes())).thenReturn(encodedPayload2);

        when(version1.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(version2.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(anotherTx.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(List.of(version1, version2, anotherTx));
        when(stagingEntityDAO.countAll()).thenReturn(3L);

        when(transactionManager.storePayload(any())).thenThrow(PrivacyViolationException.class);

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.FAILURE);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();
        verify(payloadEncoder).decode("payload1".getBytes());
        verify(payloadEncoder).decode("payload2".getBytes());

        verify(transactionManager).storePayload(encodedPayload);
        verify(transactionManager).storePayload(encodedPayload2);
    }

    @Test
    public void testRecoverSuccess() {

        final Recovery recovery = spy(Recovery.class);

        when(recovery.request()).thenReturn(RecoveryResult.SUCCESS);
        when(recovery.stage()).thenReturn(RecoveryResult.SUCCESS);
        when(recovery.sync()).thenReturn(RecoveryResult.SUCCESS);

        assertThat(recovery.recover()).isEqualTo(0);

        verify(recovery).request();
        verify(recovery).stage();
        verify(recovery).sync();
    }

    @Test
    public void testRecoverPartialSuccess() {

        final Recovery recovery = spy(Recovery.class);

        when(recovery.request()).thenReturn(RecoveryResult.PARTIAL_SUCCESS);
        when(recovery.stage()).thenReturn(RecoveryResult.PARTIAL_SUCCESS);
        when(recovery.sync()).thenReturn(RecoveryResult.SUCCESS);

        assertThat(recovery.recover()).isEqualTo(1);

        verify(recovery).request();
        verify(recovery).stage();
        verify(recovery).sync();
    }

    @Test
    public void testRecoverFailed() {

        final Recovery recovery = spy(Recovery.class);

        when(recovery.request()).thenReturn(RecoveryResult.FAILURE);
        when(recovery.stage()).thenReturn(RecoveryResult.PARTIAL_SUCCESS);
        when(recovery.sync()).thenReturn(RecoveryResult.SUCCESS);

        assertThat(recovery.recover()).isEqualTo(2);

        verify(recovery).request();
        verify(recovery).stage();
        verify(recovery).sync();
    }
}
