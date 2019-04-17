package com.quorum.tessera.jaxrs.unixsocket;

import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.client.spi.ConnectorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import java.net.URI;

public class JerseyUnixSocketConnectorProvider implements ConnectorProvider {

    @Override
    public Connector getConnector(Client client, Configuration runtimeConfig) {
        URI unixfile = (URI) runtimeConfig.getProperty("unixfile");
        return new JerseyUnixSocketConnector(unixfile);
    }

}
