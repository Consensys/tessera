package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import java.util.Set;

public class DiscoveryHelperFactory implements DiscoveryHelper {

  public static DiscoveryHelper provider() {
    NetworkStore networkStore = NetworkStore.getInstance();
    Enclave enclave = EnclaveFactory.create().enclave().get();

    return new DiscoveryHelperImpl(networkStore, enclave);
  }

  private final DiscoveryHelper discoveryHelper;

  public DiscoveryHelperFactory() {
    this(provider());
  }

  protected DiscoveryHelperFactory(DiscoveryHelper discoveryHelper) {
    this.discoveryHelper = discoveryHelper;
  }

  @Override
  public NodeInfo buildCurrent() {
    return discoveryHelper.buildCurrent();
  }

  @Override
  public void onCreate() {
    discoveryHelper.onCreate();
  }

  @Override
  public NodeInfo buildRemoteNodeInfo(PublicKey publicKey) {
    return discoveryHelper.buildRemoteNodeInfo(publicKey);
  }

  @Override
  public Set<NodeInfo> buildRemoteNodeInfos() {
    return discoveryHelper.buildRemoteNodeInfos();
  }
}
