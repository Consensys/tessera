package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.TransactionRequester;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SyncPollerTest {

    private ExecutorService executorService;

    private ResendPartyStore resendPartyStore;

    private TransactionRequester transactionRequester;

    private SyncPoller syncPoller;

    private PartyInfoService partyInfoService;

    private NodeInfoPublisher nodeInfoPublisher;

    @Before
    public void init() {
        this.executorService = mock(ExecutorService.class);
        this.resendPartyStore = mock(ResendPartyStore.class);
        this.transactionRequester = mock(TransactionRequester.class);
        this.partyInfoService = mock(PartyInfoService.class);
        this.nodeInfoPublisher = mock(NodeInfoPublisher.class);
        doReturn(true).when(nodeInfoPublisher).publishNodeInfo(anyString(), any());

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl("myurl")
            .build();
        when(partyInfoService.getPartyInfo()).thenReturn(nodeInfo);

        this.syncPoller = new SyncPoller(
            executorService, resendPartyStore, transactionRequester, partyInfoService, nodeInfoPublisher
        );
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
        verify(resendPartyStore).addUnseenParties(new HashSet<Party>());
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
        verify(nodeInfoPublisher).publishNodeInfo(eq(targetUrl), any());
        verify(partyInfoService, times(2)).getPartyInfo();
    }

    @Test
    public void localUrlIsExcludedFromPoll() {

        final String targetUrl = "localurl.com";
        final String syncableUrl = "syncable.com";
        final Party localParty = new Party(targetUrl);
        final Party syncableParty = new Party(syncableUrl);
        final Set<Party> parties = new HashSet<>(Arrays.asList(localParty, syncableParty));

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl("localurl.com")
            .withParties(parties.stream()
                .map(Party::getUrl)
                .map(com.quorum.tessera.partyinfo.node.Party::new).collect(Collectors.toList()))
            .build();

        when(partyInfoService.getPartyInfo()).thenReturn(nodeInfo);

        doReturn(Optional.empty()).when(resendPartyStore).getNextParty();

        syncPoller.run();

        verify(resendPartyStore).addUnseenParties(Set.of(syncableParty));
        verify(resendPartyStore).getNextParty();
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

        doThrow(new RuntimeException("Unable to connect")).when(nodeInfoPublisher).publishNodeInfo(anyString(), any());

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

        doReturn(false).when(nodeInfoPublisher).publishNodeInfo(anyString(), any());

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
    public void constructWithMinimalArgs() {
        assertThat(new SyncPoller(resendPartyStore, transactionRequester, partyInfoService, nodeInfoPublisher)).isNotNull();
    }
}
