package com.quorum.tessera.p2p.resend;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.partyinfo.PartyInfoParser;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.model.Party;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SyncPollerTest {

  private ExecutorService executorService;

  private ResendPartyStore resendPartyStore;

  private TransactionRequester transactionRequester;

  private SyncPoller syncPoller;

  private Discovery partyInfoService;

  private PartyInfoParser partyInfoParser;

  private P2pClient p2pClient;

  @Before
  public void init() {

    this.executorService = mock(ExecutorService.class);
    this.resendPartyStore = mock(ResendPartyStore.class);
    this.transactionRequester = mock(TransactionRequester.class);
    this.partyInfoService = mock(Discovery.class);
    this.partyInfoParser = mock(PartyInfoParser.class);
    this.p2pClient = mock(P2pClient.class);

    doReturn(true).when(p2pClient).sendPartyInfo(anyString(), any());

    NodeInfo nodeInfo = NodeInfo.Builder.create().withUrl("myurl").build();
    when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

    this.syncPoller =
        new SyncPoller(
            executorService,
            resendPartyStore,
            transactionRequester,
            partyInfoService,
            partyInfoParser,
            p2pClient);
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
    verify(partyInfoService).getCurrent();
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
    verify(partyInfoService, times(2)).getCurrent();
    verify(partyInfoParser).to(any());
    verify(p2pClient).sendPartyInfo(eq(targetUrl), any());
    verify(partyInfoService, times(2)).getCurrent();
  }

  @Test
  public void localUrlIsExcludedFromPoll() {

    final String targetUrl = "localurl.com";
    final String syncableUrl = "syncable.com";
    final com.quorum.tessera.partyinfo.node.Recipient localKey =
        com.quorum.tessera.partyinfo.node.Recipient.of(mock(PublicKey.class), targetUrl);
    final com.quorum.tessera.partyinfo.node.Recipient anotherKey =
        com.quorum.tessera.partyinfo.node.Recipient.of(mock(PublicKey.class), syncableUrl);
    final Party syncableParty = new Party(syncableUrl);
    final Set<Recipient> recipients = Set.of(localKey, anotherKey);

    NodeInfo nodeInfo =
        NodeInfo.Builder.create().withUrl("localurl.com").withRecipients(recipients).build();

    //        partyStore.store(URI.create(syncableUrl));
    when(partyInfoService.getCurrent()).thenReturn(nodeInfo);

    doReturn(Optional.empty()).when(resendPartyStore).getNextParty();

    syncPoller.run();

    verify(resendPartyStore, times(1)).addUnseenParties(Set.of(syncableParty));
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
    verify(partyInfoService, times(2)).getCurrent();
  }

  @Test
  public void singlePartyTaskUpdatePartyInfoThrowsAndNotifiesStore() {

    final String targetUrl = "fakeurl.com";
    final SyncableParty syncableParty = new SyncableParty(new Party(targetUrl), 0);

    doThrow(new RuntimeException("Unable to connect"))
        .when(p2pClient)
        .sendPartyInfo(anyString(), any());

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
    verify(partyInfoService, times(2)).getCurrent();
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
    verify(partyInfoService, times(2)).getCurrent();
  }

  @Test
  public void constructWithMinimalArgs() {

    try (var d = mockStatic(Discovery.class);
        var p = mockStatic(PartyInfoParser.class)) {
      d.when(Discovery::create).thenReturn(mock(Discovery.class));
      p.when(PartyInfoParser::create).thenReturn(mock(PartyInfoParser.class));
      assertThat(new SyncPoller(resendPartyStore, transactionRequester, p2pClient)).isNotNull();

      d.verify(Discovery::create);
      p.verify(PartyInfoParser::create);
    }
  }
}
