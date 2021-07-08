package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.discovery.Discovery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscoveryHolderImplTest {

  private DiscoveryHolderImpl discoveryHolder;

  @Before
  public void beforeTest() {
    discoveryHolder = DiscoveryHolderImpl.INSTANCE;
  }

  @After
  public void afterTest() {
    discoveryHolder.set(null);
  }

  @Test
  public void getAndSet() {
    assertThat(discoveryHolder.get()).isEmpty();
    Discovery discovery = mock(Discovery.class);
    discoveryHolder.set(discovery);
    assertThat(discoveryHolder.get()).containsSame(discovery);
  }
}
