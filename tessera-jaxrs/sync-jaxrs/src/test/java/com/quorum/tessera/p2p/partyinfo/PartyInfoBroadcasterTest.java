package com.quorum.tessera.p2p.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import jakarta.ws.rs.ProcessingException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

public class PartyInfoBroadcasterTest {

  private static final String OWN_URL = "http://own.com:8080/";

  private static final String TARGET_URL = "http://bogus.com:9878/";

  private static final String TARGET_URL_2 = "http://otherwebsite.com:9878/";

  private static final byte[] DATA = "BOGUS".getBytes();

  private Discovery discovery;

  private PartyInfoParser partyInfoParser;

  private PartyInfoBroadcaster partyInfoBroadcaster;

  private P2pClient p2pClient;

  private Executor executor;

  private PartyStore partyStore;

  @Before
  public void setUp() {
    this.discovery = mock(Discovery.class);
    this.partyInfoParser = mock(PartyInfoParser.class);
    this.p2pClient = mock(P2pClient.class);
    this.executor = mock(Executor.class);
    this.partyStore = mock(PartyStore.class);

    doAnswer(
            (InvocationOnMock invocation) -> {
              ((Runnable) invocation.getArguments()[0]).run();
              return null;
            })
        .when(executor)
        .execute(any(Runnable.class));

    when(partyInfoParser.to(any(PartyInfo.class))).thenReturn(DATA);

    this.partyInfoBroadcaster =
        new PartyInfoBroadcaster(discovery, partyInfoParser, p2pClient, executor, partyStore);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(discovery, partyInfoParser, p2pClient, partyStore);
  }

  @Test
  public void run() {
    final NodeInfo partyInfo = NodeInfo.Builder.create().withUrl(OWN_URL).build();

    when(partyStore.getParties()).thenReturn(Set.of(URI.create(OWN_URL), URI.create(TARGET_URL)));
    when(discovery.getCurrent()).thenReturn(partyInfo);
    when(p2pClient.sendPartyInfo(TARGET_URL, DATA)).thenReturn(true);

    partyInfoBroadcaster.run();
    verify(partyStore).loadFromConfigIfEmpty();
    verify(partyStore).getParties();
    verify(discovery).getCurrent();
    verify(partyInfoParser).to(any(PartyInfo.class));
    verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
  }

  @Test
  public void testWhenURLIsOwn() {
    final NodeInfo partyInfo = NodeInfo.Builder.create().withUrl(OWN_URL).build();

    when(partyStore.getParties()).thenReturn(Set.of(URI.create(OWN_URL)));

    when(discovery.getCurrent()).thenReturn(partyInfo);
    when(partyInfoParser.to(any(PartyInfo.class))).thenReturn(DATA);
    when(p2pClient.sendPartyInfo(OWN_URL, DATA)).thenReturn(true);

    partyInfoBroadcaster.run();

    verify(partyStore).loadFromConfigIfEmpty();
    verify(partyStore).getParties();
    verify(partyInfoParser).to(any(PartyInfo.class));
    verify(discovery).getCurrent();
  }

  @Test
  public void exceptionThrowByPostDoesntBubble() {

    final NodeInfo partyInfo = NodeInfo.Builder.create().withUrl(OWN_URL).build();

    when(partyStore.getParties())
        .thenReturn(Set.of(URI.create(TARGET_URL), URI.create(TARGET_URL_2)));

    doReturn(partyInfo).when(discovery).getCurrent();
    doThrow(UnsupportedOperationException.class).when(p2pClient).sendPartyInfo(TARGET_URL, DATA);

    final Throwable throwable = catchThrowable(partyInfoBroadcaster::run);

    assertThat(throwable).isNull();

    verify(partyStore).loadFromConfigIfEmpty();
    verify(partyStore).getParties();
    verify(p2pClient).sendPartyInfo(TARGET_URL, DATA);
    verify(p2pClient).sendPartyInfo(TARGET_URL_2, DATA);
    verify(discovery).getCurrent();
    verify(partyInfoParser).to(any(PartyInfo.class));
  }

  @Test
  public void constructWithMinimalArgs() {

    try (var discoveryMockedStatic = mockStatic(Discovery.class);
        var partyInfoParserMockedStatic = mockStatic(PartyInfoParser.class);
        var partyStoreMockedStatic = mockStatic(PartyStore.class)) {
      discoveryMockedStatic.when(Discovery::create).thenReturn(discovery);
      partyInfoParserMockedStatic.when(PartyInfoParser::create).thenReturn(partyInfoParser);
      partyStoreMockedStatic.when(PartyStore::getInstance).thenReturn(partyStore);

      PartyInfoBroadcaster partyInfoBroadcaster = new PartyInfoBroadcaster(mock(P2pClient.class));
      assertThat(partyInfoBroadcaster).isNotNull();

      discoveryMockedStatic.verify(Discovery::create);
      partyInfoParserMockedStatic.verify(PartyInfoParser::create);
      partyStoreMockedStatic.verify(PartyStore::getInstance);
    }
  }

  @Test
  public void jaxRsProcessingExceptionRemovesNode() {
    ProcessingException processingException = new ProcessingException("OUCH");
    CompletionException completionException = new CompletionException(processingException);

    when(p2pClient.sendPartyInfo(anyString(), any(byte[].class))).thenThrow(completionException);

    String uriData = "http://georgecowley.com/";

    when(partyStore.getParties()).thenReturn(Set.of(URI.create(uriData)));

    partyInfoBroadcaster.pollSingleParty(uriData, "somebytes".getBytes());

    verify(discovery).onDisconnect(URI.create(uriData));
    verify(partyStore).remove(URI.create(uriData));
    verify(p2pClient).sendPartyInfo(anyString(), any(byte[].class));
  }
}
