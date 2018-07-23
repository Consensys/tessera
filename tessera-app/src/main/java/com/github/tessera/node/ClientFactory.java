package com.github.tessera.node;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.ssl.context.SSLContextFactory;

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

    /**
     * Creates a new client, which may or may not be SSL enabled depending on
     * the configuration
     *
     * @param config the server configuration to specify if SSL should be used
     * @return an HTTP client capable of making outbound requests
     */
    public Client buildFrom(final ServerConfig config) {
        if (config.isSsl()) {
            final SSLContext sslContext = sslContextFactory.from(config.getSslConfig());
            return ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .build();
        } else {
            return ClientBuilder.newClient();
        }
    }

}
