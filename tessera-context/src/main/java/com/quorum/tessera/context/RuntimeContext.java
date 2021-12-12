package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import jakarta.ws.rs.client.Client;
import java.net.URI;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public interface RuntimeContext {

  Set<PublicKey> getKeys();

  KeyEncryptor getKeyEncryptor();

  List<PublicKey> getAlwaysSendTo();

  List<URI> getPeers();

  Client getP2pClient();

  boolean isRemoteKeyValidation();

  boolean isEnhancedPrivacy();

  URI getP2pServerUri();

  boolean isDisablePeerDiscovery();

  boolean isUseWhiteList();

  boolean isRecoveryMode();

  Set<PublicKey> getPublicKeys();

  boolean isOrionMode();

  boolean isMultiplePrivateStates();

  static RuntimeContext getInstance() {
    return ServiceLoaderUtil.loadSingle(ServiceLoader.load(RuntimeContext.class));
  }
}
