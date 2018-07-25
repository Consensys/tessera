package com.quorum.tessera.resteasy;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.RestServer;
import com.quorum.tessera.server.RestServerFactory;

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
