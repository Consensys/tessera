package com.github.nexus.server;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Application;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

/**
 * Implementation of a RestServer using Jersey and Grizzly.
 */
public class JerseyServer implements RestServer {

    private HttpServer server;
    
    private final URI uri;
    
    private final Application application;
    
     public JerseyServer(URI uri,Application application) {
        this.uri = Objects.requireNonNull(uri);
        this.application = Objects.requireNonNull(application);
    }

    private static final Logger LOGGER = Logger.getLogger(JerseyServer.class.getName());

    @Override
    public void start() throws Exception {

        //https://jersey.github.io/documentation/latest/appendix-properties.html
        final Map<String, Object> initParams = new HashMap<>();
        initParams.put("jersey.config.server.application.name", "Sample");
        initParams.put("jersey.config.server.tracing.type", "ON_DEMAND");
        initParams.put("jersey.config.server.tracing.threshold", "SUMMARY");
        initParams.put("jersey.config.logging.verbosity","PAYLOAD_ANY");
        initParams.put("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", "true");
        
        
        final org.glassfish.jersey.server.ResourceConfig config = 
                org.glassfish.jersey.server.ResourceConfig.forApplication(application);

        config.addProperties(initParams);

        server = GrizzlyHttpServerFactory.createHttpServer(uri, config);

        LOGGER.log(Level.INFO, "Starting {0}", uri);

        server.start();

        LOGGER.log(Level.INFO, "Started {0}", uri);

        LOGGER.log(Level.INFO, "WADL {0}/application.wadl", uri);
    }

    @Override
    public void stop() throws Exception {

        LOGGER.log(Level.INFO, "Stopping {0}", uri);

        server.shutdown();

        LOGGER.log(Level.INFO, "Stopped {0}", uri);
    }
    
}
