package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import java.net.URI;
import java.util.List;
import javax.ws.rs.client.Client;

public class MockRuntimeContext implements RuntimeContext, RuntimeContextFactory<Config> {

  private List<KeyPair> keys;

  private List<PublicKey> alwaysSendTo;

  private List<URI> peers;

  private Client p2pClient;

  private boolean remoteKeyValidation;

  private boolean enhancedPrivacy;

  private boolean disablePeerDiscovery;

  private URI p2pServerUri = URI.create("http://someurl.com");

  private KeyEncryptor keyEncryptor;

  private boolean orionMode;

  @Override
  public List<KeyPair> getKeys() {
    return keys;
  }

  @Override
  public KeyEncryptor getKeyEncryptor() {
    return keyEncryptor;
  }

  @Override
  public List<PublicKey> getAlwaysSendTo() {
    return alwaysSendTo;
  }

  @Override
  public List<URI> getPeers() {
    return peers;
  }

  @Override
  public Client getP2pClient() {
    return p2pClient;
  }

  @Override
  public boolean isRemoteKeyValidation() {
    return remoteKeyValidation;
  }

  @Override
  public boolean isEnhancedPrivacy() {
    return enhancedPrivacy;
  }

  @Override
  public URI getP2pServerUri() {
    return p2pServerUri;
  }

  @Override
  public boolean isDisablePeerDiscovery() {
    return disablePeerDiscovery;
  }

  @Override
  public boolean isUseWhiteList() {
    return false;
  }

  @Override
  public boolean isRecoveryMode() {
    return false;
  }

  @Override
  public boolean isOrionMode() {
    return orionMode;
  }

  @Override
  public boolean isMultiplePrivateStates() {
    return false;
  }

  public MockRuntimeContext setKeys(List<KeyPair> keys) {
    this.keys = keys;
    return this;
  }

  public MockRuntimeContext setAlwaysSendTo(List<PublicKey> alwaysSendTo) {
    this.alwaysSendTo = alwaysSendTo;
    return this;
  }

  public MockRuntimeContext setPeers(List<URI> peers) {
    this.peers = peers;
    return this;
  }

  public MockRuntimeContext setP2pClient(Client p2pClient) {
    this.p2pClient = p2pClient;
    return this;
  }

  public MockRuntimeContext setRemoteKeyValidation(boolean remoteKeyValidation) {
    this.remoteKeyValidation = remoteKeyValidation;
    return this;
  }

  public MockRuntimeContext setEnhancedPrivacy(boolean enhancedPrivacy) {
    this.enhancedPrivacy = enhancedPrivacy;
    return this;
  }

  public MockRuntimeContext setDisablePeerDiscovery(boolean disablePeerDiscovery) {
    this.disablePeerDiscovery = disablePeerDiscovery;
    return this;
  }

  public MockRuntimeContext setP2pServerUri(URI p2pServerUri) {
    this.p2pServerUri = p2pServerUri;
    return this;
  }

  public MockRuntimeContext setKeyEncryptor(KeyEncryptor keyEncryptor) {
    this.keyEncryptor = keyEncryptor;
    return this;
  }

  @Override
  public RuntimeContext create(Config config) {
    return this;
  }
}
