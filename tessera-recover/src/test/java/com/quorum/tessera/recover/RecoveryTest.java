package com.quorum.tessera.recover;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.sync.TransactionRequester;
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

    private TransactionRequester transactionRequester;

    private PartyInfoService partyInfoService;

    private TransactionManager transactionManager;

    @Before
    public void onSetUp() {

        partyInfoService = mock(PartyInfoService.class);
        when(partyInfoService.getPartyInfo()).thenReturn(getPartyInfo());

        transactionRequester = mock(TransactionRequester.class);
        when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(true);

        stagingEntityDAO = mock(StagingEntityDAO.class);
        transactionManager = mock(TransactionManager.class);

        this.recovery = new RecoveryImpl(stagingEntityDAO, partyInfoService, transactionRequester, transactionManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionRequester);
        verifyNoMoreInteractions(partyInfoService);
        verifyNoMoreInteractions(stagingEntityDAO);
        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void testRequestSuccess() {

        final RecoveryResult result = recovery.request();

        assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

        verify(transactionRequester, times(4)).requestAllTransactionsFromNode(anyString());
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void testRequestPartialSuccess() {

        when(transactionRequester.requestAllTransactionsFromNode(eq("http://party2/"))).thenReturn(false);

        final RecoveryResult result = recovery.request();

        assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

        verify(transactionRequester, times(4)).requestAllTransactionsFromNode(anyString());
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void testRequestFailed() {

        when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(false);

        final RecoveryResult result = recovery.request();

        assertThat(result).isEqualTo(RecoveryResult.FAILURE);

        verify(transactionRequester, times(4)).requestAllTransactionsFromNode(anyString());
        verify(partyInfoService).getPartyInfo();
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

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(transactionManager).storePayload("payload1".getBytes());
        verify(transactionManager).storePayload("payload2".getBytes());
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

        when(transactionManager.storePayload("payload1".getBytes())).thenThrow(PrivacyViolationException.class);

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(transactionManager).storePayload("payload1".getBytes());
        verify(transactionManager).storePayload("payload2".getBytes());
    }

    @Test
    public void testSyncFailed() {

        StagingTransaction version1 = mock(StagingTransaction.class);
        StagingTransaction version2 = mock(StagingTransaction.class);
        when(version1.getHash()).thenReturn("TXN1");
        when(version2.getHash()).thenReturn("TXN1");

        when(version1.getPayload()).thenReturn("payload1".getBytes());
        when(version2.getPayload()).thenReturn("payload2".getBytes());

        when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
                .thenReturn(List.of(version1, version2));

        when(stagingEntityDAO.countAll()).thenReturn(2L);

        when(transactionManager.storePayload(any())).thenThrow(PrivacyViolationException.class);

        RecoveryResult result = recovery.sync();

        assertThat(result).isEqualTo(RecoveryResult.FAILURE);

        verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
        verify(stagingEntityDAO, times(2)).countAll();

        verify(transactionManager).storePayload("payload1".getBytes());
        verify(transactionManager).storePayload("payload2".getBytes());
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

        verify(transactionManager).storePayload("payload1".getBytes());
        verify(transactionManager).storePayload("payload2".getBytes());
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
