package com.quorum.tessera.p2p.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.discovery.NodeUri;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PartyStoreFactoryTest {

  private PartyStoreFactory partyStoreFactory;

  private PartyStore partyStore;

  static final RuntimeContext runtimeContext =
      RuntimeContextFactory.newFactory().create(mock(Config.class));

  @Before
  public void beforeTest() {
    partyStore = mock(PartyStore.class);
    partyStoreFactory = new PartyStoreFactory(partyStore);

    when(runtimeContext.getP2pServerUri()).thenReturn(URI.create("http://own.com/"));
    when(runtimeContext.getPeers()).thenReturn(List.of(URI.create("http://peer.com/")));
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(partyStore);
  }

  @Test
  public void getParties() {
    partyStoreFactory.getParties();
    verify(partyStore).getParties();
  }

  @Test
  public void remove() {
    URI uri = URI.create("http://klingsor.com");
    partyStoreFactory.remove(uri);
    verify(partyStore).remove(uri);
  }

  @Test
  public void store() {
    URI uri = URI.create("http://emilsinclair.com");
    partyStoreFactory.store(uri);
    verify(partyStore).store(uri);
  }

  @Test
  public void loadFromConfigIfPartyStoreIsEmpty() {
    when(partyStore.getParties()).thenReturn(Collections.emptySet());

    partyStoreFactory.loadFromConfigIfEmpty();

    verify(partyStore).getParties();
    verify(partyStore).store(runtimeContext.getPeers().get(0));
  }

  @Test
  public void whenPeerListContainsSelf() {

    when(runtimeContext.getPeers())
        .thenReturn(List.of(URI.create("http://peer.com/"), URI.create("http://own.com/")));

    when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://own.com/")));

    partyStoreFactory.loadFromConfigIfEmpty();

    verify(partyStore).getParties();
    verify(partyStore).store(NodeUri.create("http://peer.com/").asURI());
  }

  @Test
  public void loadFromConfigIfNoPeerPresentInPartyStore() {
    when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://otherPeer.com/")));

    partyStoreFactory.loadFromConfigIfEmpty();

    verify(partyStore).getParties();
    verify(partyStore).store(runtimeContext.getPeers().get(0));
  }

  @Test
  public void doNotReloadFromConfigIfAtLeastOneConfiguredPeerPresentInPartyStore() {
    when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://peer.com/")));

    partyStoreFactory.loadFromConfigIfEmpty();

    verify(partyStore).getParties();
  }

  @Test
  public void loadFromConfigNormaliseURLsBeforeCompare() {

    when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://peer.com/")));

    when(runtimeContext.getPeers()).thenReturn(List.of(URI.create("http://peer.com")));

    partyStoreFactory.loadFromConfigIfEmpty();

    verify(partyStore).getParties();
    verify(partyStore, times(0)).store(any());
  }

  @Test
  public void provider() {
    assertThat(PartyStoreFactory.provider()).isSameAs(SimplePartyStore.INSTANCE);
  }

  @Test
  public void defaultConstructor() {
    assertThat(new PartyStoreFactory()).isNotNull();
  }
}
