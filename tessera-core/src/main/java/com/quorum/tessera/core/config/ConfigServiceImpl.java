package com.quorum.tessera.core.config;

import com.quorum.tessera.config.Config;
import java.util.Objects;


public class ConfigServiceImpl implements ConfigService {
    
    private final Config config;

    public ConfigServiceImpl(Config initialConfig) {
        this.config = Objects.requireNonNull(initialConfig);
    }

    @Override
    public Config getConfig() {
        return config;
    }

}
