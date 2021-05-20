package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DefaultNetworkStore implements NetworkStore {
  INSTANCE;

  private final Set<ActiveNode> activeNodes = ConcurrentHashMap.newKeySet();

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNetworkStore.class);

  @Override
  public NetworkStore store(ActiveNode activeNode) {

    activeNodes.removeIf(a -> a.getUri().equals(activeNode.getUri()));
    activeNodes.add(activeNode);

    LOGGER.debug("Stored node {}. Active node count {}", activeNode.getUri(), activeNodes.size());
    return this;
  }

  @Override
  public NetworkStore remove(NodeUri nodeUri) {
    activeNodes.removeIf(a -> a.getUri().equals(nodeUri));
    LOGGER.debug("Removed node {}. Active node count {}", nodeUri, activeNodes.size());
    return this;
  }

  @Override
  public Stream<ActiveNode> getActiveNodes() {
    LOGGER.debug("Fetching active nodes {}", activeNodes);
    return activeNodes.stream();
  }
}
