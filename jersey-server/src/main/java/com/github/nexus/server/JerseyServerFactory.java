package com.github.nexus.server;

import com.github.nexus.config.ServerConfig;
import javax.ws.rs.core.Application;
import java.net.URI;

public class JerseyServerFactory implements RestServerFactory{

    @Override
    public RestServer createServer(URI uri, Application aplctn, ServerConfig serverConfig) {
        return new JerseyServer(uri, aplctn, serverConfig);
    }
    
}
