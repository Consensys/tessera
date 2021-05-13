package com.quorum.tessera.discovery;

public class NetworkStoreProvider {

  public static NetworkStore provider() {
    return DefaultNetworkStore.INSTANCE;
  }
}
