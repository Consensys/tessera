package com.quorum.tessera.server;

import com.quorum.tessera.config.InfluxConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.monitoring.InfluxDbClient;
import com.quorum.tessera.server.monitoring.InfluxDbPublisher;
import com.quorum.tessera.server.monitoring.MetricsResource;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import com.quorum.tessera.ssl.context.ServerSSLContextFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Implementation of a RestServer using Jersey and Grizzly.
 */
public class JerseyServer implements TesseraServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyServer.class);

    private HttpServer server;

    private final URI uri;

    private final Application application;

    private final SSLContext sslContext;

    private final boolean secure;

    private final ScheduledExecutorService executor;

    private final InfluxConfig influxConfig;

    public JerseyServer(final ServerConfig serverConfig,final Application application) {
        this.uri = serverConfig.getBindingUri();
        this.application = Objects.requireNonNull(application);
        this.secure = serverConfig.isSsl();

        if (this.secure) {
            final SSLContextFactory sslContextFactory = ServerSSLContextFactory.create();
            this.sslContext = sslContextFactory.from(uri.toString(), serverConfig.getSslConfig());
        } else {
            this.sslContext = null;
        }

        this.executor = newSingleThreadScheduledExecutor();

        if(serverConfig.getInfluxConfig() != null) {
            this.influxConfig = serverConfig.getInfluxConfig();
        } else {
            this.influxConfig = null;
        }
    }

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
        initParams.put("jersey.config.server.monitoring.statistics.enabled", "true");
        initParams.put("jersey.config.server.monitoring.enabled", "true");
        initParams.put("jersey.config.server.monitoring.statistics.mbeans.enabled", "true");

        final ResourceConfig config = ResourceConfig.forApplication(application);
        config.addProperties(initParams)
              .register(MetricsResource.class);

        if (this.secure) {
            this.server = GrizzlyHttpServerFactory.createHttpServer(
                uri,
                new ResourceConfig(),
                true,
                new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(true),
                false
            );
        } else {
            this.server = GrizzlyHttpServerFactory.createHttpServer(uri, false);
        }

        //check if loopback was specified as binding address, if not add it
        if(!uri.getHost().matches("^localhost$|^127(?:\\.[0-9]+){0,2}\\.[0-9]+$|^(?:0*:)*?:?0*1$")) {
            this.server.addListener(new NetworkListener("localhost", "127.0.0.1", uri.getPort()));
        }

        final WebappContext ctx = new WebappContext("WebappContext");
        final ServletRegistration registration = ctx.addServlet("ServletContainer", new ServletContainer(config));
        registration.addMapping("/*");

        ctx.deploy(this.server);

        LOGGER.info("Starting {}", uri);

        this.server.start();

        LOGGER.info("Started {}", uri);
        LOGGER.info("WADL {}/application.wadl", uri);

        if(influxConfig != null) {
            startInfluxMonitoring();
        }

    }

    private void startInfluxMonitoring() {
        InfluxDbClient influxDbClient = new InfluxDbClient(this.uri, influxConfig);
        Runnable publisher = new InfluxDbPublisher(influxDbClient);

        final Runnable exceptionSafePublisher = () -> {
            try {
                publisher.run();
            } catch (final Throwable ex) {
                LOGGER.error("Error when executing action {}", publisher.getClass().getSimpleName());
                LOGGER.error("Error when executing action", ex);
            }
        };

        final long delayInSecs = influxConfig.getPushIntervalInSecs();
        this.executor.scheduleWithFixedDelay(exceptionSafePublisher, delayInSecs, delayInSecs, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Jersey server at {}", uri);

        if(influxConfig != null) {
            this.executor.shutdown();
        }

        if (Objects.nonNull(this.server)) {
            this.server.shutdown();
        }

        LOGGER.info("Stopped Jersey server at {}", uri);
    }

}
