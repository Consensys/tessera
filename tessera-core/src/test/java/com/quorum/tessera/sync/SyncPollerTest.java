package com.quorum.tessera.sync;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.sync.model.SyncableParty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

public class SyncPollerTest {

    private ExecutorService executorService;

    private ResendPartyStore resendPartyStore;

    private TransactionRequester transactionRequester;

    private SyncPoller syncPoller;

    private PartyInfoService partyInfoService;

    private PartyInfoParser partyInfoParser;

    private P2pClient p2pClient;

    @Before
    public void init() {
        this.executorService = mock(ExecutorService.class);
        this.resendPartyStore = mock(ResendPartyStore.class);
        this.transactionRequester = mock(TransactionRequester.class);
        this.partyInfoService = mock(PartyInfoService.class);
        this.partyInfoParser = mock(PartyInfoParser.class);
        this.p2pClient = mock(P2pClient.class);
        doReturn(new byte[]{}).when(p2pClient).getPartyInfo(anyString(), any());

        this.syncPoller = new SyncPoller(executorService, resendPartyStore, transactionRequester, partyInfoService, partyInfoParser, p2pClient);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(executorService, resendPartyStore, transactionRequester);
    }

    @Test
    public void noPartiesToRequestDoesntSubmitAnyTasks() {

        doReturn(Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        verify(resendPartyStore).getNextParty();
    }

    @Test
    public void singlePartySubmitsSingleTask() {

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doReturn(true).when(transactionRequester).requestAllTransactionsFromNode(targetUrl);

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester).requestAllTransactionsFromNode(targetUrl);
        verify(partyInfoService, times(1)).getPartyInfo();
        verify(partyInfoParser, times(1)).to(any());
        verify(p2pClient, times(1)).getPartyInfo(eq(targetUrl), any());

    }

    @Test
    public void singlePartyTaskFailsAndNotifiesStore() {

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doReturn(false).when(transactionRequester).requestAllTransactionsFromNode(targetUrl);

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester).requestAllTransactionsFromNode(targetUrl);
        verify(resendPartyStore).incrementFailedAttempt(syncableParty);

    }

    @Test
    public void singlePartyTaskUpdatePartyInfoThrowsAndNotifiesStore() {

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doThrow(new RuntimeException("Unable to connect")).when(p2pClient).getPartyInfo(anyString(), any());

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester, times(0)).requestAllTransactionsFromNode(targetUrl);
        verify(resendPartyStore).incrementFailedAttempt(syncableParty);

    }

    @Test
    public void singlePartyTaskUpdatePartyInfoFailsAndNotifiesStore() {

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doReturn(null).when(p2pClient).getPartyInfo(anyString(), any());

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester, times(0)).requestAllTransactionsFromNode(targetUrl);
        verify(resendPartyStore).incrementFailedAttempt(syncableParty);

    }

}
