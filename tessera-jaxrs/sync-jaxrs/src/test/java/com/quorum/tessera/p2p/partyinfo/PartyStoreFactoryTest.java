package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.context.RuntimeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PartyStoreFactoryTest {

    private PartyStoreFactory partyStoreFactory;

    private PartyStore partyStore;

    @Before
    public void beforeTest() {
        partyStore = mock(PartyStore.class);
        partyStoreFactory = new PartyStoreFactory(partyStore);
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

    @Ignore
    @Test
    public void loadFromConfigIfPartyStoreIsEmpty() {
        when(partyStore.getParties()).thenReturn(Collections.emptySet());

        partyStoreFactory.loadFromConfigIfEmpty();

        verify(partyStore).getParties();
        verify(partyStore).store(RuntimeContext.getInstance().getPeers().get(0));
    }

    @Ignore
    @Test
    public void loadFromConfigIfNoPeerPresentInPartyStore() {
        when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://otherPeer.com/")));

        partyStoreFactory.loadFromConfigIfEmpty();

        verify(partyStore).getParties();
        verify(partyStore).store(RuntimeContext.getInstance().getPeers().get(0));
    }

    @Ignore
    @Test
    public void doNotReloadFromConfigIfAtLeastOneConfiguredPeerPresentInPartyStore() {
        when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://peer.com/")));

        partyStoreFactory.loadFromConfigIfEmpty();

        verify(partyStore).getParties();
    }

    @Ignore
    @Test
    public void loadFromConfigNormaliseURLsBeforeCompare() {

        when(partyStore.getParties()).thenReturn(Set.of(URI.create("http://peer.com/")));

        when(RuntimeContext.getInstance().getPeers()).thenReturn(List.of(URI.create("http://peer.com")));

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
