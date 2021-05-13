package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraApp;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Creates Grizzly and Jersey implementations of the {@link TesseraServer} */
public class JerseyServerFactory implements TesseraServerFactory<Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyServerFactory.class);

  @Override
  public TesseraServer createServer(ServerConfig serverConfig, Set<Object> services) {
    LOGGER.debug("Creating JAXRS application {}", serverConfig);
    Application application =
        services.stream()
            .filter(TesseraApp.class::isInstance)
            .filter(Application.class::isInstance)
            .map(TesseraApp.class::cast)
            .filter(a -> a.getAppType().equals(serverConfig.getApp()))
            .map(Application.class::cast)
            .findFirst()
            .get();

    LOGGER.debug("Created JAXRS application {}", application);

    return new JerseyServer(serverConfig, application);
  }

  @Override
  public CommunicationType communicationType() {
    return CommunicationType.REST;
  }
}
