package com.github.nexus.ssl;

import com.github.nexus.config.SslConfig;
import java.util.ServiceLoader;
import javax.net.ssl.SSLContext;

public interface SSLContextFactory {
    
    static SSLContextFactory create() {
        return ServiceLoader.load(SSLContextFactory.class).iterator().next();
    }
    
    
     SSLContext from(SslConfig sslConfig);

}
