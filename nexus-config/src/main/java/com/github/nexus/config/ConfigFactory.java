package com.github.nexus.config;

import java.util.Properties;
import java.util.ServiceLoader;

public interface ConfigFactory {
    
    static ConfigFactory create() {
        return ServiceLoader.load(ConfigFactory.class).iterator().next();
    }
        
    Config create(Properties properties);

}
