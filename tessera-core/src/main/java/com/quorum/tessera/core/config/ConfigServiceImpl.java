package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.util.ConfigFileStore;
import java.util.List;
import java.util.Objects;

public class ConfigServiceImpl implements ConfigService {
    
    private final Config config;

    private final ConfigFileStore configFileStore;
    
    public ConfigServiceImpl(Config initialConfig,ConfigFileStore configFileStore) {
        this.config = Objects.requireNonNull(initialConfig);
        this.configFileStore = Objects.requireNonNull(configFileStore);
    }
 
    @Override
    public Config getConfig() {
        return config;
    }
    
    @Override
    public void addPeer(String url) {
        this.config.addPeer(new Peer(url));
        configFileStore.save(config);                
    }

    @Override
    public List<Peer> getPeers() {
        return config.getPeers();
    }

}
