package com.github.tessera.ssl.context;

import com.github.tessera.config.SslConfig;

import javax.net.ssl.SSLContext;

public interface SSLContextFactory {

    SSLContext from(SslConfig sslConfig);

}
