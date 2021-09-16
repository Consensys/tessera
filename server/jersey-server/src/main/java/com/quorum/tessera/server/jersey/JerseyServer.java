package com.quorum.tessera.server.jersey;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.InfluxConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.jaxrs.CorsDomainResponseFilter;
import com.quorum.tessera.server.jaxrs.LoggingFilter;
import com.quorum.tessera.server.monitoring.InfluxDbClient;
import com.quorum.tessera.server.monitoring.InfluxDbPublisher;
import com.quorum.tessera.server.monitoring.MetricsResource;
import com.quorum.tessera.server.utils.ServerUtils;
import jakarta.ws.rs.core.Application;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/** Implementation of a RestServer using Jersey and Jetty. */
public class JerseyServer implements TesseraServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyServer.class);

  private org.eclipse.jetty.server.Server server;

  private final URI uri;

  private final Class<? extends Application> application;

  private final ScheduledExecutorService executor;

  private final InfluxConfig influxConfig;

  private final ServerConfig serverConfig;

  private final AppType type;

  public JerseyServer(
      final ServerConfig serverConfig, final Class<? extends Application> application) {
    LOGGER.debug("Constructing from {} and {}", serverConfig, application);
    this.uri = serverConfig.getServerUri();
    this.application = Objects.requireNonNull(application);
    this.serverConfig = Objects.requireNonNull(serverConfig);

    this.executor = newSingleThreadScheduledExecutor();

    this.influxConfig = serverConfig.getInfluxConfig();
    this.type = serverConfig.getApp();
    LOGGER.debug("Constructed {}", this);
  }

  @Override
  public void start() throws Exception {
    LOGGER.debug("Starting {}", this);

    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    // https://jersey.github.io/documentation/latest/appendix-properties.html
    final Map<String, Object> initParams = new HashMap<>();
    initParams.put("jersey.config.server.application.name", application.getSimpleName());
    initParams.put("jersey.config.server.tracing.type", "ON_DEMAND");
    initParams.put("jersey.config.server.tracing.threshold", "SUMMARY");
    initParams.put("jersey.config.logging.verbosity", "PAYLOAD_ANY");
    initParams.put("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", "true");
    initParams.put("jersey.config.server.monitoring.statistics.enabled", "true");
    initParams.put("jersey.config.server.monitoring.enabled", "true");
    initParams.put("jersey.config.server.monitoring.statistics.mbeans.enabled", "true");

    final ResourceConfig config = ResourceConfig.forApplicationClass(application);

    config.addProperties(initParams).register(MetricsResource.class).register(LoggingFilter.class);

    if (serverConfig.getCrossDomainConfig() != null && !serverConfig.isUnixSocket()) {
      config.register(new CorsDomainResponseFilter(serverConfig.getCrossDomainConfig()));
    }

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bind(ServiceLoaderInjectionResolver.class).to(JustInTimeInjectionResolver.class);
          }
        });

    LOGGER.debug("Building Server from {}", serverConfig);
    this.server = ServerUtils.buildWebServer(serverConfig);
    LOGGER.debug("Built Server from {}", serverConfig);

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
  }

  private void startInfluxMonitoring() {
    InfluxDbClient influxDbClient = new InfluxDbClient(uri, influxConfig, type);
    Runnable publisher = new InfluxDbPublisher(influxDbClient);

    final Runnable exceptionSafePublisher =
        () -> {
          try {
            publisher.run();
          } catch (final Throwable ex) {
            LOGGER.error(
                "Error when executing action {}, exception details:",
                publisher.getClass().getSimpleName(),
                ex);
          }
        };

    final long delayInSecs = influxConfig.getPushIntervalInSecs();
    this.executor.scheduleWithFixedDelay(
        exceptionSafePublisher, delayInSecs, delayInSecs, TimeUnit.SECONDS);
  }

  @Override
  public void stop() {
    LOGGER.info("Stopping Jersey server at {}", uri);

    if (influxConfig != null) {
      this.executor.shutdown();
    }

    if (Objects.nonNull(this.server)) {
      try {
        this.server.stop();
      } catch (Exception ex) {
        LOGGER.warn(null, ex);
      }
    }

    LOGGER.info("Stopped Jersey server at {}", uri);
  }
}
