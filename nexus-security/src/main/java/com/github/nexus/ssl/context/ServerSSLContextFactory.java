package com.github.nexus.ssl.context;

public interface ServerSSLContextFactory extends SSLContextFactory {
    
    static ServerSSLContextFactory create() {
        return new ServerSSLContextFactoryImpl();
    }

}
