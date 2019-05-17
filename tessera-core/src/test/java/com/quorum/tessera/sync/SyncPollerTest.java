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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.Collections.emptySet;
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
        doReturn(true).when(p2pClient).sendPartyInfo(anyString(), any());
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

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doReturn(true).when(transactionRequester).requestAllTransactionsFromNode(targetUrl);

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();
        verify(resendPartyStore).addUnseenParties(emptySet());

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester).requestAllTransactionsFromNode(targetUrl);
        verify(partyInfoService, times(2)).getPartyInfo();
        verify(partyInfoParser).to(any());
        verify(p2pClient).sendPartyInfo(eq(targetUrl), any());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void localUrlIsExcludedFromPoll() {

        final String targetUrl = "localurl.com";
        final String syncableUrl = "syncable.com";
        final Party localParty = new Party(targetUrl);
        final Party syncableParty = new Party(syncableUrl);
        final Set<Party> parties = new HashSet<>(Arrays.asList(localParty, syncableParty));
        when(partyInfoService.getPartyInfo())
            .thenReturn(new PartyInfo("localurl.com", emptySet(), parties));
        
        doReturn(Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();
        
        verify(resendPartyStore, times(1)).addUnseenParties(new HashSet<>(Arrays.asList(syncableParty)));
        verify(resendPartyStore, times(1)).getNextParty();
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
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void singlePartyTaskUpdatePartyInfoThrowsAndNotifiesStore() {

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doThrow(new RuntimeException("Unable to connect")).when(p2pClient).sendPartyInfo(anyString(), any());

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester, times(0)).requestAllTransactionsFromNode(targetUrl);
        verify(resendPartyStore).incrementFailedAttempt(syncableParty);
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void singlePartyTaskUpdatePartyInfoFailsAndNotifiesStore() {

        final String targetUrl = "fakeurl.com";
        final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

        doReturn(false).when(p2pClient).sendPartyInfo(anyString(), any());

        doReturn(Optional.of(syncableParty), Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorService).submit(captor.capture());
        verify(resendPartyStore, times(2)).getNextParty();

        final Runnable task = captor.getValue();
        task.run();

        verify(transactionRequester, times(0)).requestAllTransactionsFromNode(targetUrl);
        verify(resendPartyStore).incrementFailedAttempt(syncableParty);
        verify(resendPartyStore).addUnseenParties(emptySet());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

}
