package com.quorum.tessera.recovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.data.staging.StagingTransaction;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.recovery.RecoveryResult;
import com.quorum.tessera.recovery.RecoveryTestCase;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import jakarta.persistence.PersistenceException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RecoveryImplTest extends RecoveryTestCase {

  private Recovery recovery;

  private StagingEntityDAO stagingEntityDAO;

  private BatchTransactionRequester transactionRequester;

  private Discovery discovery;

  private TransactionManager transactionManager;

  @Before
  public void onSetUp() {

    discovery = mock(Discovery.class);
    when(discovery.getCurrent()).thenReturn(getCurrent());
    when(discovery.getRemoteNodeInfos()).thenReturn(getAllNodeInfos());

    transactionRequester = mock(BatchTransactionRequester.class);
    when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(true);

    stagingEntityDAO = mock(StagingEntityDAO.class);
    transactionManager = mock(TransactionManager.class);

    this.recovery =
        new RecoveryImpl(stagingEntityDAO, discovery, transactionRequester, transactionManager);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(transactionRequester);
    verifyNoMoreInteractions(discovery);
    verifyNoMoreInteractions(stagingEntityDAO);
    verifyNoMoreInteractions(transactionManager);
  }

  @Test
  public void testRequestSuccess() {

    when(transactionRequester.requestAllTransactionsFromLegacyNode(anyString())).thenReturn(true);

    final RecoveryResult result = recovery.request();

    assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

    verify(transactionRequester).requestAllTransactionsFromNode("http://party1/");
    verify(transactionRequester).requestAllTransactionsFromNode("http://party3/");

    verify(transactionRequester).requestAllTransactionsFromLegacyNode("http://party2/");
    verify(transactionRequester).requestAllTransactionsFromLegacyNode("http://party4/");

    verify(discovery).getRemoteNodeInfos();
  }

  @Test
  public void testRequestPartialSuccess() {

    when(transactionRequester.requestAllTransactionsFromLegacyNode(eq("http://party2")))
        .thenReturn(false);

    final RecoveryResult result = recovery.request();

    assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

    verify(transactionRequester, times(2)).requestAllTransactionsFromNode(anyString());
    verify(transactionRequester, times(2)).requestAllTransactionsFromLegacyNode(anyString());
    verify(discovery).getRemoteNodeInfos();
  }

  @Test
  public void testRequestFailed() {

    when(transactionRequester.requestAllTransactionsFromNode(anyString())).thenReturn(false);
    when(transactionRequester.requestAllTransactionsFromLegacyNode(anyString())).thenReturn(false);

    final RecoveryResult result = recovery.request();

    assertThat(result).isEqualTo(RecoveryResult.FAILURE);

    verify(transactionRequester, times(2)).requestAllTransactionsFromNode(anyString());
    verify(transactionRequester, times(2)).requestAllTransactionsFromLegacyNode(anyString());
    verify(discovery).getRemoteNodeInfos();
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

    EncodedPayload firstPayload = mock(EncodedPayload.class);
    EncodedPayload secondPayload = mock(EncodedPayload.class);

    when(version1.getEncodedPayload()).thenReturn(firstPayload);
    when(version2.getEncodedPayload()).thenReturn(secondPayload);

    when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
        .thenReturn(List.of(version1, version2));
    when(stagingEntityDAO.countAll()).thenReturn(2L);

    when(transactionManager.storePayload(any())).thenReturn(new MessageHash("hash".getBytes()));

    RecoveryResult result = recovery.sync();

    assertThat(result).isEqualTo(RecoveryResult.SUCCESS);

    verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
    verify(stagingEntityDAO, times(2)).countAll();

    verify(transactionManager).storePayload(firstPayload);
    verify(transactionManager).storePayload(secondPayload);
  }

  @Test
  public void testSyncPartialSuccess() {

    StagingTransaction version1 = mock(StagingTransaction.class);
    StagingTransaction version2 = mock(StagingTransaction.class);

    when(version1.getHash()).thenReturn("TXN1");
    when(version2.getHash()).thenReturn("TXN1");

    when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
        .thenReturn(List.of(version1, version2));
    when(stagingEntityDAO.countAll()).thenReturn(2L);

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    EncodedPayload encodedPayload2 = mock(EncodedPayload.class);

    when(version1.getEncodedPayload()).thenReturn(encodedPayload);
    when(version2.getEncodedPayload()).thenReturn(encodedPayload2);

    when(transactionManager.storePayload(encodedPayload))
        .thenThrow(PrivacyViolationException.class);

    RecoveryResult result = recovery.sync();

    assertThat(result).isEqualTo(RecoveryResult.PARTIAL_SUCCESS);

    verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
    verify(stagingEntityDAO, times(2)).countAll();

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

    when(version1.getEncodedPayload()).thenReturn(encodedPayload);
    when(version2.getEncodedPayload()).thenReturn(encodedPayload2);

    List<StagingTransaction> stagingTransactions = List.of(version1, version2);

    when(stagingEntityDAO.retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt()))
        .thenReturn(stagingTransactions);

    when(stagingEntityDAO.countAll()).thenReturn((long) stagingTransactions.size());

    when(transactionManager.storePayload(any())).thenThrow(PrivacyViolationException.class);

    RecoveryResult result = recovery.sync();

    assertThat(result).isEqualTo(RecoveryResult.FAILURE);

    verify(stagingEntityDAO).retrieveTransactionBatchOrderByStageAndHash(anyInt(), anyInt());
    verify(stagingEntityDAO, times(2)).countAll();

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

    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    EncodedPayload encodedPayload2 = mock(EncodedPayload.class);

    when(version1.getEncodedPayload()).thenReturn(encodedPayload);
    when(version2.getEncodedPayload()).thenReturn(encodedPayload);
    when(anotherTx.getEncodedPayload()).thenReturn(encodedPayload2);

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

    verify(transactionManager).storePayload(encodedPayload);
    verify(transactionManager).storePayload(encodedPayload2);
  }

  @Test
  public void testRecoverSuccess() {

    final Recovery spy = spy(recovery);

    doReturn(RecoveryResult.SUCCESS).when(spy).request();
    doReturn(RecoveryResult.SUCCESS).when(spy).stage();
    doReturn(RecoveryResult.SUCCESS).when(spy).sync();

    assertThat(spy.recover()).isEqualTo(0);

    verify(spy).request();
    verify(spy).stage();
    verify(spy).sync();

    verify(stagingEntityDAO).countAll();
    verify(stagingEntityDAO).countAllAffected();
  }

  @Test
  public void testRecoverPartialSuccess() {

    final Recovery spy = spy(recovery);

    doReturn(RecoveryResult.PARTIAL_SUCCESS).when(spy).request();
    doReturn(RecoveryResult.PARTIAL_SUCCESS).when(spy).stage();
    doReturn(RecoveryResult.SUCCESS).when(spy).sync();

    assertThat(spy.recover()).isEqualTo(1);

    verify(spy).request();
    verify(spy).stage();
    verify(spy).sync();

    verify(stagingEntityDAO).countAll();
    verify(stagingEntityDAO).countAllAffected();
  }

  @Test
  public void testRecoverFailed() {

    final Recovery spy = spy(recovery);

    doReturn(RecoveryResult.FAILURE).when(spy).request();
    doReturn(RecoveryResult.PARTIAL_SUCCESS).when(spy).stage();
    doReturn(RecoveryResult.SUCCESS).when(spy).sync();

    assertThat(spy.recover()).isEqualTo(2);

    verify(spy).request();
    verify(spy).stage();
    verify(spy).sync();

    verify(stagingEntityDAO).countAll();
    verify(stagingEntityDAO).countAllAffected();
  }

  @Test
  public void testDBStagingTxNotEmpty() {

    final Recovery spy = spy(recovery);

    when(stagingEntityDAO.countAll()).thenReturn(1L);

    assertThat(spy.recover()).isEqualTo(2);

    verify(stagingEntityDAO).countAll();
  }

  @Test
  public void testDBStagingAffectedTxNotEmpty() {

    final Recovery spy = spy(recovery);

    when(stagingEntityDAO.countAllAffected()).thenReturn(1L);

    assertThat(spy.recover()).isEqualTo(2);

    verify(stagingEntityDAO).countAll();
    verify(stagingEntityDAO).countAllAffected();
  }

  @Test
  public void testDBTableNotExisted() {

    final Recovery spy = spy(recovery);

    when(stagingEntityDAO.countAll()).thenThrow(new PersistenceException("OUCH"));

    assertThat(spy.recover()).isEqualTo(2);

    verify(stagingEntityDAO).countAll();
  }
}
