package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DiscoveryHolderTest {

  @Test
  public void create() {
    DiscoveryHolder discoveryHolder = DiscoveryHolder.create();
    assertThat(discoveryHolder).isSameAs(DiscoveryHolderImpl.INSTANCE);
  }
}
