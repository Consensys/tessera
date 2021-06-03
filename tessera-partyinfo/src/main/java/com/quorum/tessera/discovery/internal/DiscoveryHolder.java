package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.Discovery;
import java.util.Optional;

public interface DiscoveryHolder {

  void set(Discovery discovery);

  Optional<Discovery> get();

  static DiscoveryHolder create() {
    return DiscoveryHolderImpl.INSTANCE;
  }
}
