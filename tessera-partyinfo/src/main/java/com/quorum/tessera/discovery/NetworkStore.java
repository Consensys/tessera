package com.quorum.tessera.discovery;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface NetworkStore {

  NetworkStore store(ActiveNode activeNode);

  NetworkStore remove(NodeUri nodeUri);

  Stream<ActiveNode> getActiveNodes();

  static NetworkStore getInstance() {
    return ServiceLoader.load(NetworkStore.class).findFirst().get();
  }
}
