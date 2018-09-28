package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import java.util.List;

public interface ConfigService {
    
    Config getConfig();
    
    void addPeer(String url);
    
    List<Peer> getPeers();

     
}
