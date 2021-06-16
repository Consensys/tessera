package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.ActiveNode;
import com.quorum.tessera.discovery.DiscoveryHelper;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.discovery.NodeUri;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.KeyNotFoundException;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import com.quorum.tessera.version.ApiVersion;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryHelperImpl implements DiscoveryHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryHelperImpl.class);

  private Enclave enclave;

  private final NetworkStore networkStore;

  public DiscoveryHelperImpl(NetworkStore networkStore, Enclave enclave) {
    this.networkStore = networkStore;
    this.enclave = enclave;
  }

  @Override
  public void onCreate() {
    RuntimeContext runtimeContext = RuntimeContext.getInstance();

    final NodeUri nodeUri =
        Optional.of(runtimeContext).map(RuntimeContext::getP2pServerUri).map(NodeUri::create).get();

    ActiveNode thisNode =
        ActiveNode.Builder.create()
            .withUri(nodeUri)
            .withKeys(enclave.getPublicKeys())
            .withSupportedVersions(ApiVersion.versions())
            .build();

    networkStore.store(thisNode);
  }

  @Override
  public NodeInfo buildCurrent() {

    final URI uri = RuntimeContext.getInstance().getP2pServerUri();
    final NodeUri nodeUri = NodeUri.create(uri);
    final List<ActiveNode> activeNodes = networkStore.getActiveNodes().collect(Collectors.toList());

    Set<Recipient> recipients =
        activeNodes.stream()
            .filter(a -> !a.getKeys().isEmpty())
            .flatMap(a -> a.getKeys().stream().map(k -> Recipient.of(k, a.getUri().asString())))
            .collect(Collectors.toSet());

    NodeInfo nodeInfo =
        NodeInfo.Builder.create()
            .withRecipients(recipients)
            .withUrl(nodeUri.asString())
            .withSupportedApiVersions(ApiVersion.versions())
            .build();

    LOGGER.debug("Built nodeinfo {}", nodeInfo);
    return nodeInfo;
  }

  @Override
  public NodeInfo buildRemoteNodeInfo(PublicKey recipientKey) {

    final ActiveNode activeNode =
        networkStore
            .getActiveNodes()
            .filter(node -> node.getKeys().contains(recipientKey))
            .findAny()
            .orElseThrow(
                () ->
                    new KeyNotFoundException(
                        "Recipient not found for key: " + recipientKey.encodeToBase64()));

    final String nodeUrl = activeNode.getUri().asString();

    final Set<Recipient> recipients =
        activeNode.getKeys().stream()
            .map(k -> Recipient.of(k, nodeUrl))
            .collect(Collectors.toSet());

    final NodeInfo nodeInfo =
        NodeInfo.Builder.create()
            .withUrl(nodeUrl)
            .withRecipients(recipients)
            .withSupportedApiVersions(activeNode.getSupportedVersions())
            .build();

    return nodeInfo;
  }

  @Override
  public Set<NodeInfo> buildRemoteNodeInfos() {

    final NodeUri uri = NodeUri.create(RuntimeContext.getInstance().getP2pServerUri());

    return networkStore
        .getActiveNodes()
        .filter(n -> !n.getUri().equals(uri))
        .map(
            activeNode -> {
              String url = activeNode.getUri().asString();
              return NodeInfo.Builder.create()
                  .withUrl(url)
                  .withRecipients(
                      activeNode.getKeys().stream()
                          .map(k -> Recipient.of(k, url))
                          .collect(Collectors.toSet()))
                  .withSupportedApiVersions(activeNode.getSupportedVersions())
                  .build();
            })
        .collect(Collectors.toSet());
  }
}
