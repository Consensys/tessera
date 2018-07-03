package com.github.nexus.ssl.context;

import com.github.nexus.config.SslConfig;

import javax.net.ssl.SSLContext;

public interface ServerSSLContextFactory {
    
    static ServerSSLContextFactory create() {
        return new ServerSSLContextFactoryImpl();
    }
    
     SSLContext from(SslConfig sslConfig);

}
