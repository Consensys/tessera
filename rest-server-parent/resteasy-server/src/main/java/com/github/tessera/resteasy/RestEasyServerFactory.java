
package com.github.tessera.resteasy;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.server.RestServer;
import com.github.tessera.server.RestServerFactory;

import javax.ws.rs.core.Application;
import java.net.URI;

public class RestEasyServerFactory implements RestServerFactory {

    @Override
    public RestServer createServer(URI uri, Application application, ServerConfig serverConfig) {
        return new RestEasyServer(uri, application, serverConfig);
    }
    
}
