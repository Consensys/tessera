package com.github.nexus.server;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.ServiceLoader;


public interface RestServerFactory {
    
    RestServer createServer(URI uri, Application application, SSLContext sslContext, boolean secure);
    
    static RestServerFactory create() {
        return ServiceLoader.load(RestServerFactory.class)
                .iterator().next();
    }
    
}
