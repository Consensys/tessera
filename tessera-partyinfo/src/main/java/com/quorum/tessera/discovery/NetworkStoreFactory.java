package com.quorum.tessera.discovery;

import java.util.stream.Stream;

public class NetworkStoreFactory implements NetworkStore {

  public static NetworkStore provider() {
    return DefaultNetworkStore.INSTANCE;
  }

  private final NetworkStore networkStore;

  public NetworkStoreFactory() {
    this.networkStore = provider();
  }

  @Override
  public NetworkStore store(ActiveNode activeNode) {
    return networkStore.store(activeNode);
  }

  @Override
  public NetworkStore remove(NodeUri nodeUri) {
    return networkStore.remove(nodeUri);
  }

  @Override
  public Stream<ActiveNode> getActiveNodes() {
    return networkStore.getActiveNodes();
  }
}
