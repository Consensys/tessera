package com.github.nexus.ssl.context;

import com.github.nexus.config.SslConfig;

import javax.net.ssl.SSLContext;
import java.util.ServiceLoader;

public interface SSLContextFactory {
    
    static SSLContextFactory create() {
        return ServiceLoader.load(SSLContextFactory.class).iterator().next();
    }
    
    
     SSLContext from(SslConfig sslConfig);

}
