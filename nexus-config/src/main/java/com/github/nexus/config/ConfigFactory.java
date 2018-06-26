
package com.github.nexus.config;

import java.io.InputStream;
import java.util.ServiceLoader;


public interface ConfigFactory {

    Config create(InputStream inputStream);

    static ConfigFactory create() {
        return ServiceLoader.load(ConfigFactory.class)
                .iterator().next();
    
    }
    
    
}
