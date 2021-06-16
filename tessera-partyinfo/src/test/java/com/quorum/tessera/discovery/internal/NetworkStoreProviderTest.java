package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NetworkStoreProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new NetworkStoreProvider()).isNotNull();
  }

  @Test
  public void provider() {
    assertThat(NetworkStoreProvider.provider())
        .isNotNull()
        .isExactlyInstanceOf(DefaultNetworkStore.class);
  }
}
