package com.quorum.tessera.p2p.partyinfo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
