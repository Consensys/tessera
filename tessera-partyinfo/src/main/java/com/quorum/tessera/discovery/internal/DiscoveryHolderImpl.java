package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.Discovery;
import java.util.Optional;

enum DiscoveryHolderImpl implements DiscoveryHolder {
  INSTANCE;

  private Discovery discovery;

  @Override
  public void set(Discovery discovery) {
    this.discovery = discovery;
  }

  @Override
  public Optional<Discovery> get() {
    return Optional.ofNullable(discovery);
  }
}
