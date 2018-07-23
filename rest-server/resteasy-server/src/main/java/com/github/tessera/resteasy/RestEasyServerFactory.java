package com.github.tessera.resteasy;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.server.RestServer;
import com.github.tessera.server.RestServerFactory;

import javax.ws.rs.core.Application;
import java.net.URI;

/**
 * Creates RestEasy and Sun HTTP server implementations of the {@link RestServer}
 */
public class RestEasyServerFactory implements RestServerFactory {

    @Override
    public RestServer createServer(URI uri, Application application, ServerConfig serverConfig) {
        return new RestEasyServer(uri, application, serverConfig);
    }
    
}
