package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import javax.net.ssl.SSLContext;

public interface SSLContextFactory {

  SSLContext from(String address, SslConfig sslConfig);
}
