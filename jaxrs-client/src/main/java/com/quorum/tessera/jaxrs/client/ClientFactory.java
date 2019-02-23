package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.UnixServerSocket;
import com.quorum.tessera.jaxrs.unixsocket.JerseyUnixSocketConnectorProvider;
import com.quorum.tessera.ssl.context.SSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Objects;
import org.glassfish.jersey.client.ClientConfig;

/**
 * Creates HTTP clients that can make requests
 * <p>
 * Makes the client SSL enabled if specified
 */
public class ClientFactory {

    private final SSLContextFactory sslContextFactory;

    public ClientFactory(final SSLContextFactory sslContextFactory) {
        this.sslContextFactory = Objects.requireNonNull(sslContextFactory);
    }

    public ClientFactory() {
        this(com.quorum.tessera.ssl.context.ClientSSLContextFactory.create());
    }

    /**
     * Creates a new client, which may or may not be SSL enabled depending on
     * the configuration
     *
     * @see Client
     */
    public Client buildFrom(final ServerConfig config) {
        //TODO: Remove buld time dependency. 
        if (UnixServerSocket.class.isInstance(config.getServerSocket())) {
            JerseyUnixSocketConnectorProvider connectorProvider = new JerseyUnixSocketConnectorProvider();

            ClientConfig clientConfig = new ClientConfig();
            clientConfig.connectorProvider(connectorProvider);
            
            return ClientBuilder.newClient(clientConfig);

        } else if (config.isSsl()) {
            final SSLContext sslContext = sslContextFactory.from(
                    config.getServerUri().toString(),
                    config.getSslConfig());
            return ClientBuilder.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } else {
            return ClientBuilder.newClient();
        }
    }


}
