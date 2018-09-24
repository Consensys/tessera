package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;


public class ConfigServiceImpl implements ConfigService {
    
    private volatile Config config;

    public ConfigServiceImpl(Config initialConfig) {
        this.config = initialConfig;
    }

    @Override
    public Config getConfig() {
        return config;
    }
    
}
