package com.github.nexus.ssl.context;

import com.github.nexus.config.SslConfig;

import javax.net.ssl.SSLContext;

public interface SSLContextFactory {

    SSLContext from(SslConfig sslConfig);

}
