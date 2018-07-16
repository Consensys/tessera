package com.github.tessera.node;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.ssl.context.SSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Objects;

public class ClientFactory {

    private final SSLContextFactory sslContextFactory;

    public ClientFactory(SSLContextFactory sslContextFactory) {
        this.sslContextFactory = Objects.requireNonNull(sslContextFactory);
    }
    
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
