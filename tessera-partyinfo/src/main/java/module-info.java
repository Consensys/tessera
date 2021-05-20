module tessera.partyinfo {
  requires org.apache.commons.lang3;
  requires org.slf4j;
  requires tessera.config;
  requires tessera.enclave.api;
  requires tessera.encryption.api;
  requires tessera.shared;
  requires tessera.context;

  exports com.quorum.tessera.discovery;
  exports com.quorum.tessera.partyinfo;
  exports com.quorum.tessera.partyinfo.node;

  uses com.quorum.tessera.discovery.NetworkStore;
  uses com.quorum.tessera.enclave.Enclave;
  uses com.quorum.tessera.discovery.DiscoveryHelper;
  uses com.quorum.tessera.discovery.Discovery;
  uses com.quorum.tessera.partyinfo.P2pClient;

  provides com.quorum.tessera.discovery.Discovery with
      com.quorum.tessera.discovery.internal.DiscoveryProvider;
  provides com.quorum.tessera.discovery.DiscoveryHelper with
      com.quorum.tessera.discovery.internal.DiscoveryHelperProvider;
  provides com.quorum.tessera.discovery.EnclaveKeySynchroniser with
      com.quorum.tessera.discovery.internal.EnclaveKeySynchroniserProvider;
  provides com.quorum.tessera.discovery.NetworkStore with
      com.quorum.tessera.discovery.internal.NetworkStoreProvider;
}
