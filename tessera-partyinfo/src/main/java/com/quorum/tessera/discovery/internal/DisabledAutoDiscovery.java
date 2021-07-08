package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.AutoDiscoveryDisabledException;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DisabledAutoDiscovery implements Discovery {

  private final NetworkStore networkStore;

  private final Set<NodeUri> knownPeers;

  public DisabledAutoDiscovery(NetworkStore networkStore, Set<NodeUri> knownPeers) {
    this.networkStore = Objects.requireNonNull(networkStore);
    this.knownPeers = knownPeers;
  }

  @Override
  public void onUpdate(NodeInfo nodeInfo) {

    if (!knownPeers.contains(NodeUri.create(nodeInfo.getUrl()))) {
      throw new AutoDiscoveryDisabledException(
          String.format("%s is not a known peer", nodeInfo.getUrl()));
    }

    final NodeUri callerNodeUri = NodeUri.create(nodeInfo.getUrl());

    final Set<PublicKey> keys =
        nodeInfo.getRecipients().stream()
            .filter(r -> NodeUri.create(r.getUrl()).equals(callerNodeUri))
            .map(Recipient::getKey)
            .collect(Collectors.toSet());

    final ActiveNode activeNode =
        ActiveNode.Builder.create()
            .withUri(callerNodeUri)
            .withSupportedVersions(nodeInfo.supportedApiVersions())
            .withKeys(keys)
            .build();

    networkStore.store(activeNode);
  }

  @Override
  public void onDisconnect(URI nodeUri) {
    networkStore.remove(NodeUri.create(nodeUri));
  }
}
