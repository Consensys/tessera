package com.quorum.tessera.server;

import com.quorum.tessera.config.ServerConfig;
import javax.ws.rs.core.Application;
import java.net.URI;

/**
 * Creates Grizzly and Jersey implementations of the {@link RestServer}
 */
public class JerseyServerFactory implements RestServerFactory{

    @Override
    public RestServer createServer(URI uri, Application aplctn, ServerConfig serverConfig) {
        return new JerseyServer(uri, aplctn, serverConfig);
    }
    
}
