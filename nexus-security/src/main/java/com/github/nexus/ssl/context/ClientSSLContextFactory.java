package com.github.nexus.ssl.context;

import com.github.nexus.config.SslConfig;

import javax.net.ssl.SSLContext;

public interface ClientSSLContextFactory {

    static ClientSSLContextFactory create() {
        return new ClientSSLContextFactoryImpl();
    }

    SSLContext from(SslConfig sslConfig);
}
