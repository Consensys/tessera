package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    boolean isOrionMode();

    default Set<PublicKey> getPublicKeys() {
        return getKeys().stream().map(KeyPair::getPublicKey).collect(Collectors.toSet());
    }

    static RuntimeContext getInstance() {
        return ContextHolder.getInstance().getContext().get();
    }
}
