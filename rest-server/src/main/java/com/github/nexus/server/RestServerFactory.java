package com.github.nexus.server;

import com.github.nexus.config.ServerConfig;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.ServiceLoader;


public interface RestServerFactory {
    
    RestServer createServer(URI uri, Application application, ServerConfig serverConfig);
    
    static RestServerFactory create() {
        return ServiceLoader.load(RestServerFactory.class)
                .iterator().next();
    }
    
}
