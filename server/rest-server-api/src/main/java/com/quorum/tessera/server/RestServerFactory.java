package com.quorum.tessera.server;

import com.quorum.tessera.config.ServerConfig;

import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.ServiceLoader;

/**
 * A factory for creating HTTP Servers using the provided configuration
 */
public interface RestServerFactory {

    /**
     * Create an implementation specific HTTP server
     *
     * @param uri          the full URI that the node should be accessible from
     * @param application  the application that is sitting on top of the HTTP server
     * @param serverConfig the server configuration that contains info such as port and SSL config
     * @return the implementation of the HTTP server, in a stopped state
     */
    RestServer createServer(URI uri, Application application, ServerConfig serverConfig);

    /**
     * Finds an implementation of the factory from the service loader
     *
     * @return an instance of RestServerFactory interface
     */
    static RestServerFactory create() {
        return ServiceLoader.load(RestServerFactory.class).iterator().next();
    }

}
