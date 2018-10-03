package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Peer;
import java.net.URI;
import java.util.List;

public interface ConfigService {

    void addPeer(String url);
    
    List<Peer> getPeers();

    boolean isUseWhiteList();

    boolean isDisablePeerDiscovery();
    
    URI getServerUri();
     
}
