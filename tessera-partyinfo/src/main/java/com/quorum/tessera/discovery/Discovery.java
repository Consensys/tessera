package com.quorum.tessera.discovery;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import java.net.URI;
import java.util.ServiceLoader;
import java.util.Set;

public interface Discovery {

  default void onCreate() {
    DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
    discoveryHelper.onCreate();
  }

  void onUpdate(NodeInfo nodeInfo);

  void onDisconnect(URI nodeUri);

  default NodeInfo getCurrent() {
    DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
    return discoveryHelper.buildCurrent();
  }

  default NodeInfo getRemoteNodeInfo(PublicKey publicKey) {
    DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
    return discoveryHelper.buildRemoteNodeInfo(publicKey);
  }

  default Set<NodeInfo> getRemoteNodeInfos() {
    DiscoveryHelper discoveryHelper = DiscoveryHelper.create();
    return discoveryHelper.buildRemoteNodeInfos();
  }

  static Discovery create() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(Discovery.class));
  }
}
