package com.quorum.tessera.q2t;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import java.net.URI;

public class MockDiscovery implements Discovery {

  @Override
  public void onCreate() {}

  @Override
  public void onUpdate(NodeInfo nodeInfo) {}

  @Override
  public void onDisconnect(URI nodeUri) {}

  @Override
  public NodeInfo getRemoteNodeInfo(PublicKey publicKey) {
    return mock(NodeInfo.class);
  }
}
