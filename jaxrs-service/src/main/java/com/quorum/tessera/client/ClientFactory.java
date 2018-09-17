package com.quorum.tessera.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.ssl.context.SSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Objects;

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
        if (config.isSsl()) {
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
