package com.quorum.tessera.server;

import com.quorum.tessera.config.InfluxConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.UnixServerSocket;
import com.quorum.tessera.server.monitoring.InfluxDbClient;
import com.quorum.tessera.server.monitoring.InfluxDbPublisher;
import com.quorum.tessera.server.monitoring.MetricsResource;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import com.quorum.tessera.ssl.context.ServerSSLContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Implementation of a RestServer using Jersey and Jetty.
 */
public class JerseyServer implements TesseraServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyServer.class);

    private org.eclipse.jetty.server.Server server;

    private final URI uri;

    private final Application application;

    private final SSLContext sslContext;

    private final boolean secure;

    private final ScheduledExecutorService executor;

    private final InfluxConfig influxConfig;

    private final UnixServerSocket unixServerSocket;

    public JerseyServer(final ServerConfig serverConfig, final Application application) {
        this.uri = serverConfig.getBindingUri();
        this.application = Objects.requireNonNull(application);
        this.secure = serverConfig.isSsl();

        this.unixServerSocket = Optional.of(serverConfig)
                .map(ServerConfig::getServerSocket)
                .filter(s -> UnixServerSocket.class.isInstance(s))
                .map(UnixServerSocket.class::cast)
                .orElse(null);

        if (this.secure) {
            final SSLContextFactory sslContextFactory = ServerSSLContextFactory.create();
            this.sslContext = sslContextFactory.from(uri.toString(), serverConfig.getSslConfig());
        } else {
            this.sslContext = null;
        }

        this.executor = newSingleThreadScheduledExecutor();

        if (serverConfig.getInfluxConfig() != null) {
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

        this.server = new Server();

        if (Objects.nonNull(unixServerSocket)) {

            HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory();
            
            org.eclipse.jetty.unixsocket.UnixSocketConnector connector = 
                    new org.eclipse.jetty.unixsocket.UnixSocketConnector(server,httpConnectionFactory);
            connector.setAcceptQueueSize(128);
            
            connector.setUnixSocket(unixServerSocket.getPath());

            server.setConnectors(new Connector[]{connector});

        } else if (this.secure) {
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());

            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setSslContext(sslContext);
            ServerConnector connector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https));
            connector.setPort(uri.getPort());
            server.setConnectors(new Connector[]{connector});

        } else {
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(uri.getPort());
            server.setConnectors(new Connector[]{connector});

        }

        ServletContextHandler context = new ServletContextHandler(server, "/");
        ServletContainer servletContainer = new ServletContainer(config);
        ServletHolder jerseyServlet = new ServletHolder(servletContainer);

        context.addServlet(jerseyServlet, "/*");

        LOGGER.info("Starting {}", uri);

        this.server.start();

        LOGGER.info("Started {}", uri);
        LOGGER.info("WADL {}/application.wadl", uri);

        if (influxConfig != null) {
            startInfluxMonitoring();
        }

       // server.join();

    }

    private void startInfluxMonitoring() {
        InfluxDbClient influxDbClient = new InfluxDbClient(this.uri, influxConfig);
        Runnable publisher = new InfluxDbPublisher(influxDbClient);

        final Runnable exceptionSafePublisher = () -> {
            try{
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

        if (influxConfig != null) {
            this.executor.shutdown();
        }

        if (Objects.nonNull(this.server)) {
            try{
                this.server.stop();
            } catch (Exception ex) {
               LOGGER.warn(null, ex);
            }
        }

        LOGGER.info("Stopped Jersey server at {}", uri);
    }

}
