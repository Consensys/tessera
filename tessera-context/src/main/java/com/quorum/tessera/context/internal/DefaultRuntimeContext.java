package com.quorum.tessera.context.internal;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.ws.rs.client.Client;
import java.net.URI;
import java.util.List;
import java.util.Set;

class DefaultRuntimeContext implements RuntimeContext {

  private final Set<PublicKey> keys;

  private final KeyEncryptor keyEncryptor;

  private final List<PublicKey> alwaysSendTo;

  private final List<URI> peers;

  private final Client p2pClient;

  private final boolean remoteKeyValidation;

  private final boolean enhancedPrivacy;

  private final URI p2pServerUri;

  private final boolean disablePeerDiscovery;

  private final boolean useWhiteList;

  private final boolean recoveryMode;

  private final boolean orionMode;

  private final boolean multiplePrivateStates;

  protected DefaultRuntimeContext(
      Set<PublicKey> keys,
      KeyEncryptor keyEncryptor,
      List<PublicKey> alwaysSendTo,
      List<URI> peers,
      Client p2pClient,
      boolean remoteKeyValidation,
      boolean enhancedPrivacy,
      URI p2pServerUri,
      boolean disablePeerDiscovery,
      boolean useWhiteList,
      boolean recoveryMode,
      boolean orionMode,
      boolean multiplePrivateStates) {
    this.keys = Set.copyOf(keys);
    this.keyEncryptor = keyEncryptor;
    this.alwaysSendTo = List.copyOf(alwaysSendTo);
    this.peers = List.copyOf(peers);
    this.p2pClient = p2pClient;
    this.remoteKeyValidation = remoteKeyValidation;
    this.enhancedPrivacy = enhancedPrivacy;
    this.p2pServerUri = p2pServerUri;
    this.disablePeerDiscovery = disablePeerDiscovery;
    this.useWhiteList = useWhiteList;
    this.recoveryMode = recoveryMode;
    this.orionMode = orionMode;
    this.multiplePrivateStates = multiplePrivateStates;
  }

  public Set<PublicKey> getKeys() {
    return Set.copyOf(keys);
  }

  public KeyEncryptor getKeyEncryptor() {
    return keyEncryptor;
  }

  public List<PublicKey> getAlwaysSendTo() {
    return alwaysSendTo;
  }

  public List<URI> getPeers() {
    return peers;
  }

  public Client getP2pClient() {
    return p2pClient;
  }

  public boolean isRemoteKeyValidation() {
    return remoteKeyValidation;
  }

  @Override
  public boolean isEnhancedPrivacy() {
    return enhancedPrivacy;
  }

  public URI getP2pServerUri() {
    return p2pServerUri;
  }

  @Override
  public boolean isDisablePeerDiscovery() {
    return disablePeerDiscovery;
  }

  @Override
  public boolean isUseWhiteList() {
    return useWhiteList;
  }

  @Override
  public boolean isRecoveryMode() {
    return recoveryMode;
  }

  @Override
  public Set<PublicKey> getPublicKeys() {
    return Set.copyOf(this.keys);
  }

  @Override
  public boolean isOrionMode() {
    return orionMode;
  }

  @Override
  public boolean isMultiplePrivateStates() {
    return multiplePrivateStates;
  }

  @Override
  public String toString() {
    return "DefaultRuntimeContext{"
        + "keys="
        + keys
        + ", keyEncryptor="
        + keyEncryptor
        + ", alwaysSendTo="
        + alwaysSendTo
        + ", peers="
        + peers
        + ", p2pClient="
        + p2pClient
        + ", remoteKeyValidation="
        + remoteKeyValidation
        + ", enhancedPrivacy="
        + enhancedPrivacy
        + ", p2pServerUri="
        + p2pServerUri
        + ", disablePeerDiscovery="
        + disablePeerDiscovery
        + ", useWhiteList="
        + useWhiteList
        + ", recoveryMode="
        + recoveryMode
        + ", orionMode="
        + orionMode
        + ", multiplePrivateStates="
        + multiplePrivateStates
        + '}';
  }
}
