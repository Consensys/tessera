package com.github.nexus.server;

import java.net.URI;
import java.util.ServiceLoader;
import javax.ws.rs.core.Application;


public interface RestServerFactory {
    
    RestServer createServer(URI uri, Application application);
    
    static RestServerFactory create() {
        return ServiceLoader.load(RestServerFactory.class)
                .iterator().next();
    }
    
}
