package com.quorum.tessera.context.internal;

import com.quorum.tessera.config.ClientMode;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import jakarta.ws.rs.client.Client;
import java.net.URI;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeContextBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeContextBuilder.class);

  private Set<PublicKey> keys = new HashSet<>();

  private KeyEncryptor keyEncryptor;

  private List<PublicKey> alwaysSendTo = new ArrayList<>();

  private List<URI> peers = new ArrayList<>();

  private Client p2pClient;

  private URI p2pServerUri;

  private boolean remoteKeyValidation;

  private boolean enhancedPrivacy;

  private boolean disablePeerDiscovery;

  private boolean useWhiteList;

  private boolean recoveryMode;

  private ClientMode clientMode;

  private boolean multiplePrivateStates;

  private RuntimeContextBuilder() {}

  public static RuntimeContextBuilder create() {
    return new RuntimeContextBuilder();
  }

  public RuntimeContextBuilder withP2pServerUri(URI p2pServerUri) {
    this.p2pServerUri = p2pServerUri;
    return this;
  }

  public RuntimeContextBuilder withKeys(Set<PublicKey> keys) {
    this.keys.addAll(keys);
    return this;
  }

  public RuntimeContextBuilder withKeyEncryptor(KeyEncryptor keyEncryptor) {
    this.keyEncryptor = keyEncryptor;
    return this;
  }

  public RuntimeContextBuilder withPeers(List<URI> peers) {
    this.peers.addAll(peers);
    return this;
  }

  public RuntimeContextBuilder withAlwaysSendTo(List<PublicKey> alwaysSendTo) {
    this.alwaysSendTo.addAll(alwaysSendTo);
    return this;
  }

  public RuntimeContextBuilder withP2pClient(Client p2pClient) {
    this.p2pClient = p2pClient;
    return this;
  }

  public RuntimeContextBuilder withRemoteKeyValidation(boolean remoteKeyValidation) {
    this.remoteKeyValidation = remoteKeyValidation;
    return this;
  }

  public RuntimeContextBuilder withEnhancedPrivacy(boolean enhancedPrivacy) {
    this.enhancedPrivacy = enhancedPrivacy;
    return this;
  }

  public RuntimeContextBuilder withDisablePeerDiscovery(boolean disablePeerDiscovery) {
    this.disablePeerDiscovery = disablePeerDiscovery;
    return this;
  }

  public RuntimeContextBuilder withUseWhiteList(boolean useWhiteList) {
    this.useWhiteList = useWhiteList;
    return this;
  }

  public RuntimeContextBuilder withRecoveryMode(boolean recoveryMode) {
    this.recoveryMode = recoveryMode;
    return this;
  }

  public RuntimeContextBuilder withClientMode(ClientMode clientMode) {
    this.clientMode = clientMode;
    return this;
  }

  public RuntimeContextBuilder withMultiplePrivateStates(boolean multiplePrivateStates) {
    this.multiplePrivateStates = multiplePrivateStates;
    return this;
  }

  public RuntimeContext build() {

    LOGGER.debug("Building {}", this);

    Objects.requireNonNull(p2pServerUri, "No p2pServerUri provided. ");
    Objects.requireNonNull(keyEncryptor, "No key encryptor provided. ");
    Objects.requireNonNull(p2pClient, "No p2pClient provided. ");

    RuntimeContext instance =
        new DefaultRuntimeContext(
            keys,
            keyEncryptor,
            alwaysSendTo,
            peers,
            p2pClient,
            remoteKeyValidation,
            enhancedPrivacy,
            p2pServerUri,
            disablePeerDiscovery,
            useWhiteList,
            recoveryMode,
            clientMode == ClientMode.ORION,
            multiplePrivateStates);
    LOGGER.debug("Built {}", this);
    return instance;
  }
}
