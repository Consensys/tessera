package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoDiscovery implements Discovery {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutoDiscovery.class);

  private final NetworkStore networkStore;

  public AutoDiscovery(NetworkStore networkStore) {
    this.networkStore = Objects.requireNonNull(networkStore);
  }

  @Override
  public void onUpdate(final NodeInfo nodeInfo) {

    LOGGER.debug("Processing node info {}", nodeInfo);

    final NodeUri callerNodeUri = NodeUri.create(nodeInfo.getUrl());

    LOGGER.debug("Update node {}", callerNodeUri);

    final Set<PublicKey> keys =
        nodeInfo.getRecipients().stream()
            .filter(r -> NodeUri.create(r.getUrl()).equals(callerNodeUri))
            .map(Recipient::getKey)
            .collect(Collectors.toSet());

    final ActiveNode activeNode =
        ActiveNode.Builder.create()
            .withUri(callerNodeUri)
            .withKeys(keys)
            .withSupportedVersions(nodeInfo.supportedApiVersions())
            .build();

    networkStore.store(activeNode);
  }

  @Override
  public void onDisconnect(URI nodeUri) {
    networkStore.remove(NodeUri.create(nodeUri));
  }
}
