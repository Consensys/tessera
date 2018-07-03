package com.github.nexus.node;

import com.github.nexus.config.ServerConfig;
import com.github.nexus.ssl.context.SSLContextFactory;

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
