package com.quorum.tessera.transaction.batch;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.transaction.BatchResendManager;
import com.quorum.tessera.transaction.SyncState;
import com.quorum.tessera.transaction.TransactionRequester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;

public class BatchResendSyncTest {

    private PartyInfoService partyInfoService;

    private P2pClient p2pClient;

    private BatchResendManager batchResendManager;

    private ProcessControl processControl;

    private TransactionRequester transactionRequester;

    private BatchResendSync batchResendSync;

    private SyncState syncState;

    @Before
    public void init() {
        partyInfoService = mock(PartyInfoService.class);
        p2pClient = mock(P2pClient.class);
        batchResendManager = mock(BatchResendManager.class);
        processControl = mock(ProcessControl.class);
        transactionRequester = mock(TransactionRequester.class);
        syncState = mock(SyncState.class);
        when(transactionRequester.requestAllTransactionsFromNode(any())).thenReturn(true);

        batchResendSync =
                new BatchResendSync(
                        partyInfoService, batchResendManager, 1, processControl, transactionRequester, syncState);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(
                partyInfoService, p2pClient, batchResendManager, processControl, transactionRequester, syncState);
    }

    @Test
    public void processEarlyExit() {
        batchResendSync.setMustStop(true);
        batchResendSync.run();
        verify(processControl).exit(ProcessControl.STOPPED);
        verify(syncState).setResendMode(true);
    }

    @Test
    public void processRunsThroughAllStages() {

        when(partyInfoService.getPartyInfo())
                .thenReturn(
                        new PartyInfo(
                                "url3",
                                Collections.emptySet(),
                                new HashSet<>(
                                        Arrays.asList(new Party("url1"), new Party("url2/"), new Party("url3/")))));

        batchResendSync.run();
        verify(batchResendManager).cleanupStagingArea();
        verify(transactionRequester, times(2)).requestAllTransactionsFromNode(any());
        verify(batchResendManager).performStaging();
        verify(batchResendManager).performSync();
        verify(partyInfoService).getPartyInfo();
        verify(syncState).setResendMode(true);

        verify(processControl).exit(ProcessControl.SUCCESS);
    }

    @Test
    public void partialResendSuccess() {

        when(partyInfoService.getPartyInfo())
                .thenReturn(
                        new PartyInfo(
                                "url3/",
                                Collections.emptySet(),
                                new HashSet<>(Arrays.asList(new Party("url1"), new Party("url2")))));
        when(transactionRequester.requestAllTransactionsFromNode(any())).thenReturn(true).thenReturn(false);

        batchResendSync.run();
        verify(batchResendManager).cleanupStagingArea();
        verify(transactionRequester, times(2)).requestAllTransactionsFromNode(any());
        verify(batchResendManager).performStaging();
        verify(batchResendManager).performSync();
        verify(partyInfoService).getPartyInfo();
        verify(syncState).setResendMode(true);

        verify(processControl).exit(ProcessControl.SUCCESS);
    }

    @Test
    public void resendFailure() {

        when(partyInfoService.getPartyInfo())
                .thenReturn(
                        new PartyInfo(
                                "url",
                                Collections.emptySet(),
                                new HashSet<>(Arrays.asList(new Party("url1"), new Party("url2")))));
        when(transactionRequester.requestAllTransactionsFromNode(any())).thenReturn(false);

        batchResendSync.run();
        verify(batchResendManager).cleanupStagingArea();
        verify(transactionRequester, times(2)).requestAllTransactionsFromNode(any());
        verify(batchResendManager).performStaging();
        verify(batchResendManager).performSync();
        verify(partyInfoService).getPartyInfo();
        verify(syncState).setResendMode(true);

        verify(processControl).exit(ProcessControl.SUCCESS);
    }

    @Test
    public void exceptionInCleanup() {

        when(partyInfoService.getPartyInfo())
                .thenReturn(
                        new PartyInfo(
                                "url",
                                Collections.emptySet(),
                                new HashSet<>(Arrays.asList(new Party("url1"), new Party("url2")))));

        doThrow(new RuntimeException("Unable to clean")).when(batchResendManager).cleanupStagingArea();

        batchResendSync.run();

        verify(batchResendManager).cleanupStagingArea();
        verify(syncState).setResendMode(true);

        verify(processControl).exit(ProcessControl.FAILURE);
    }

    @Test
    public void processIsStoppedByShutdownHook() {
        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        batchResendSync.start();
        verify(processControl).start(any(Runnable.class), argumentCaptor.capture());
        argumentCaptor.getValue().run();
        batchResendSync.run();

        verify(syncState).setResendMode(true);

        verify(processControl).exit(ProcessControl.STOPPED);
    }

    @Test
    public void createDefaultInstance() {
        assertThat(new BatchResendSync(partyInfoService, batchResendManager, 0, processControl, transactionRequester))
                .isNotNull();
    }
}
