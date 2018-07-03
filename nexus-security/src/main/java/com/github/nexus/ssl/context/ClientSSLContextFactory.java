package com.github.nexus.ssl.context;

public interface ClientSSLContextFactory extends SSLContextFactory {

    static ClientSSLContextFactory create() {
        return new ClientSSLContextFactoryImpl();
    }

}
