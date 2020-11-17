package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public interface RuntimeContext {

    List<KeyPair> getKeys();

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

    static RuntimeContext getInstance() {
        return ServiceLoader.load(RuntimeContext.class).findFirst().get();
    }
}
