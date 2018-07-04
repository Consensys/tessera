package com.github.nexus.ssl.context;

import java.util.ServiceLoader;

public interface ServerSSLContextFactory extends SSLContextFactory {
    
    static SSLContextFactory create() {
        return ServiceLoader.load(ServerSSLContextFactory.class).iterator().next();
    }

}
