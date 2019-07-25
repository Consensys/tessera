package com.quorum.tessera.admin;

import com.quorum.tessera.config.FeatureToggles;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.encryption.PublicKey;

import java.net.URI;
import java.util.List;
import java.util.Set;

public interface ConfigService {

    void addPeer(String url);

    List<Peer> getPeers();

    boolean isUseWhiteList();

    boolean isDisablePeerDiscovery();

    URI getServerUri();

    Set<PublicKey> getPublicKeys();

    FeatureToggles featureToggles();
}
