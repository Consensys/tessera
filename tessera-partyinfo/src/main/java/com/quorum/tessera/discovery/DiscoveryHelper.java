package com.quorum.tessera.discovery;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import java.util.ServiceLoader;
import java.util.Set;

public interface DiscoveryHelper {

  void onCreate();

  NodeInfo buildCurrent();

  NodeInfo buildRemoteNodeInfo(PublicKey publicKey);

  Set<NodeInfo> buildRemoteNodeInfos();

  static DiscoveryHelper getInstance() {
    return ServiceLoader.load(DiscoveryHelper.class).findFirst().get();
  }
}
