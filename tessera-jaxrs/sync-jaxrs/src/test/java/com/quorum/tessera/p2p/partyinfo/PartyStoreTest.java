package com.quorum.tessera.p2p.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.context.RuntimeContext;
import java.net.URI;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PartyStoreTest {

  @Before
  @After
  public void beforeAndAfterTest() {
    PartyStore simplePartyStore = PartyStore.getInstance();
    simplePartyStore.getParties().forEach(simplePartyStore::remove);

    assertThat(simplePartyStore.getParties()).isEmpty();
  }

  @Test
  public void addReadAndRemove() {

    PartyStore simplePartyStore = PartyStore.getInstance();
    URI uri = URI.create("http://walterlindrum.com/");
    simplePartyStore.store(uri);
    assertThat(simplePartyStore.getParties()).containsExactly(uri);
    simplePartyStore.remove(uri);
    assertThat(simplePartyStore.getParties()).isEmpty();
  }

  @Test
  public void loadFromConfigIfEmpty() {

    URI peerUri = URI.create("somepeer");

    try (var runtimeContextMockedStatic = mockStatic(RuntimeContext.class)) {
      RuntimeContext runtimeContext = mock(RuntimeContext.class);
      when(runtimeContext.getPeers()).thenReturn(List.of(URI.create("somepeer")));

      runtimeContextMockedStatic.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      PartyStore partyStore = PartyStore.getInstance();
      partyStore.loadFromConfigIfEmpty();

      assertThat(partyStore.getParties()).containsExactly(peerUri);
    }
  }

  @Test
  public void loadFromConfigIfEmptyExistingParties() {

    URI peerUri = URI.create("somepeer");
    URI existingPeerUri = URI.create("anexistingpeer");
    try (var runtimeContextMockedStatic = mockStatic(RuntimeContext.class)) {
      RuntimeContext runtimeContext = mock(RuntimeContext.class);
      when(runtimeContext.getPeers()).thenReturn(List.of(peerUri));

      runtimeContextMockedStatic.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

      PartyStore partyStore = PartyStore.getInstance();

      partyStore.store(existingPeerUri);
      partyStore.loadFromConfigIfEmpty();

      assertThat(partyStore.getParties()).containsExactlyInAnyOrder(peerUri, existingPeerUri);
    }
  }
}
