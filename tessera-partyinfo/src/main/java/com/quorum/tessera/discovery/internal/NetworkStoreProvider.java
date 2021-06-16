package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.NetworkStore;

public class NetworkStoreProvider {

  public static NetworkStore provider() {
    return DefaultNetworkStore.INSTANCE;
  }
}
