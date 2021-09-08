package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.*;
import com.quorum.tessera.enclave.EnclaveClient;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import jakarta.ws.rs.client.Client;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveClientProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveClientProvider.class);

  public static EnclaveClient provider() {

    Config config = ConfigFactory.create().getConfig();

    LOGGER.debug("Creating RestfulEnclaveClient with {}", config);
    Optional<ServerConfig> enclaveServerConfig =
        config.getServerConfigs().stream().filter(sc -> sc.getApp() == AppType.ENCLAVE).findAny();

    final ClientFactory clientFactory = new ClientFactory();

    LOGGER.debug("Creating server context from config");
    ServerConfig serverConfig = enclaveServerConfig.get();
    LOGGER.debug("Created server context from config");

    Client client = clientFactory.buildFrom(serverConfig);
    LOGGER.info("Creating remoted enclave for {}", serverConfig.getServerUri());
    return new RestfulEnclaveClient(client, serverConfig.getServerUri());
  }
}
