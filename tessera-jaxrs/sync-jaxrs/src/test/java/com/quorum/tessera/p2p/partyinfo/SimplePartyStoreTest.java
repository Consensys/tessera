package com.quorum.tessera.p2p.partyinfo;

import com.quorum.tessera.p2p.partyinfo.SimplePartyStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class SimplePartyStoreTest {

    @Before
    @After
    public void beforeAndAfterTest() {
        SimplePartyStore simplePartyStore = SimplePartyStore.INSTANCE;
        simplePartyStore.getParties().forEach(simplePartyStore::remove);

        assertThat(simplePartyStore.getParties()).isEmpty();
    }

    @Test
    public void addReadAndRemove() {

        SimplePartyStore simplePartyStore = SimplePartyStore.INSTANCE;
        URI uri = URI.create("http://walterlindrum.com/");
        simplePartyStore.store(uri);
        assertThat(simplePartyStore.getParties()).containsExactly(uri);
        simplePartyStore.remove(uri);
        assertThat(simplePartyStore.getParties()).isEmpty();
    }
}
