package com.quorum.tessera.jaxrs.unixsocket;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import java.net.URI;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

public class JerseyUnixSocketConnectorProvider implements ConnectorProvider {

  @Override
  public Connector getConnector(Client client, Configuration runtimeConfig) {
    URI unixfile = (URI) runtimeConfig.getProperty("unixfile");
    return new JerseyUnixSocketConnector(unixfile);
  }
}
