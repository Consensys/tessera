
package com.github.nexus.resteasy;

import com.github.nexus.config.ServerConfig;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;

import javax.ws.rs.core.Application;
import java.net.URI;

public class RestEasyServerFactory implements RestServerFactory {

    @Override
    public RestServer createServer(URI uri, Application application, ServerConfig serverConfig) {
        return new RestEasyServer(uri, application, serverConfig);
    }
    
}
