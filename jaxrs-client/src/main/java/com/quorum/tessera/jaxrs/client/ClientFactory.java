package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.UnixServerSocket;
import com.quorum.tessera.reflect.ReflectCallback;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Objects;
import javax.ws.rs.core.Configuration;

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

    private static Configuration createUnixServerSocketConfig() {

        return ReflectCallback.execute(() -> {
            Class configType = Class.forName("org.glassfish.jersey.client.ClientConfig");
            Class providerClass = Class.forName("com.quorum.tessera.jaxrs.unixsocket.JerseyUnixSocketConnectorProvider");
            Object config = configType.newInstance();
            Object provider = providerClass.newInstance();
            Method connectorProviderMethod = Arrays.stream(configType.getDeclaredMethods())
                    .filter(m -> m.getName().equals("connectorProvider")).findFirst().get();
                    
            connectorProviderMethod.invoke(config, provider);
            
            return Configuration.class.cast(config);
        });
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
            Configuration clientConfig = createUnixServerSocketConfig();
            Path unixfile = Paths.get(UnixServerSocket.class.cast(config.getServerSocket()).getPath());
            return ClientBuilder.newClient(clientConfig)
                    .property("unixfile", unixfile);

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
