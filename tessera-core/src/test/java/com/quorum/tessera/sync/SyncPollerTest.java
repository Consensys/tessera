package com.quorum.tessera.sync;

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

    @Before
    public void init() {
        this.executorService = mock(ExecutorService.class);
        this.resendPartyStore = mock(ResendPartyStore.class);
        this.transactionRequester = mock(TransactionRequester.class);

        this.syncPoller = new SyncPoller(executorService, resendPartyStore, transactionRequester);
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

}
