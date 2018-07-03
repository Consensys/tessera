package com.github.nexus.ssl.context;

import java.util.ServiceLoader;

public interface ClientSSLContextFactory extends SSLContextFactory {

    static SSLContextFactory create() {
        return ServiceLoader.load(ClientSSLContextFactory.class).iterator().next();
    }

}
