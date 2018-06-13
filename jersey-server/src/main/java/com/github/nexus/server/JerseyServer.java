package com.github.nexus.server;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.core.Application;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Implementation of a RestServer using Jersey and Grizzly.
 */
public class JerseyServer implements RestServer {

    private HttpServer server;

    private final URI uri;

    private final Application application;

    public JerseyServer(URI uri, Application application) {
        this.uri = Objects.requireNonNull(uri);
        this.application = Objects.requireNonNull(application);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyServer.class);

    @Override
    public void start() throws Exception {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        //https://jersey.github.io/documentation/latest/appendix-properties.html
        final Map<String, Object> initParams = new HashMap<>();
        initParams.put("jersey.config.server.application.name", application.getClass().getSimpleName());
        initParams.put("jersey.config.server.tracing.type", "ON_DEMAND");
        initParams.put("jersey.config.server.tracing.threshold", "SUMMARY");
        initParams.put("jersey.config.logging.verbosity", "PAYLOAD_ANY");
        initParams.put("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", "true");
        initParams.put("jersey.config.server.monitoring.statistics.enabled","true");
        initParams.put("jersey.config.server.monitoring.enabled", "true");
        initParams.put("jersey.config.server.monitoring.statistics.mbeans.enabled","true");
        
        
        
        final org.glassfish.jersey.server.ResourceConfig config
                = org.glassfish.jersey.server.ResourceConfig.forApplication(application);

        config.addProperties(initParams);

        server = GrizzlyHttpServerFactory.createHttpServer(uri, config);

        LOGGER.info("Starting {}", uri);

        server.start();

        LOGGER.info("Started {}", uri);

        LOGGER.info("WADL {}/application.wadl", uri);
    }

    @Override
    public void stop() throws Exception {

        LOGGER.info("Stopping {}", uri);
        if (Objects.nonNull(server)) {
            server.shutdown();
        }
        LOGGER.info("Stopped {}", uri);
    }

}
