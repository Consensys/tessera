package com.quorum.tessera.sync;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.Party;
import com.quorum.tessera.node.model.PartyInfo;
import com.quorum.tessera.sync.model.SyncableParty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.*;

public class SyncPollerTest {

    private static final URI TARGET = URI.create("fakeurl.com");

    private static final SyncableParty PARTY = new SyncableParty(new Party(TARGET), 0);

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

        when(p2pClient.getPartyInfo(any(URI.class), any())).thenReturn(new byte[0]);
        when(partyInfoService.getPartyInfo()).thenReturn(new PartyInfo("myurl", emptySet(), emptySet()));

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
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService).getPartyInfo();
    }

    @Test
    public void singlePartySubmitsSingleTask() {

        doReturn(true).when(transactionRequester).requestAllTransactionsFromNode(TARGET);

        doReturn(Optional.of(PARTY), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();
        verify(resendPartyStore).addUnseenParties(emptySet());

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester).requestAllTransactionsFromNode(TARGET);
        verify(partyInfoService, times(2)).getPartyInfo();
        verify(partyInfoParser).to(any());
        verify(p2pClient).getPartyInfo(eq(TARGET), any());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void singlePartyTaskFailsAndNotifiesStore() {

        doReturn(false).when(transactionRequester).requestAllTransactionsFromNode(TARGET);

        doReturn(Optional.of(PARTY), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester).requestAllTransactionsFromNode(TARGET);
        verify(resendPartyStore).incrementFailedAttempt(PARTY);
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void singlePartyTaskUpdatePartyInfoThrowsAndNotifiesStore() {

        doThrow(new RuntimeException("Unable to connect")).when(p2pClient).getPartyInfo(any(URI.class), any());

        doReturn(Optional.of(PARTY), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester, times(0)).requestAllTransactionsFromNode(TARGET);
        verify(resendPartyStore).incrementFailedAttempt(PARTY);
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void singlePartyTaskUpdatePartyInfoFailsAndNotifiesStore() {

        doReturn(null).when(p2pClient).getPartyInfo(any(URI.class), any());

        doReturn(Optional.of(PARTY), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester, times(0)).requestAllTransactionsFromNode(TARGET);
        verify(resendPartyStore).incrementFailedAttempt(PARTY);
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

}
