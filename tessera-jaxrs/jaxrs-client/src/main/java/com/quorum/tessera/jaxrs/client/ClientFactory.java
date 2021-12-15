package com.quorum.tessera.jaxrs.client;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.IntervalPropertyHelper;
import com.quorum.tessera.context.RestClientFactory;
import com.quorum.tessera.reflect.ReflectCallback;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Configuration;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;

/**
 * Creates HTTP clients that can make requests
 *
 * <p>Makes the client SSL enabled if specified
 */
public class ClientFactory implements RestClientFactory {

  private final SSLContextFactory sslContextFactory;

  public ClientFactory(final SSLContextFactory sslContextFactory) {
    this.sslContextFactory = Objects.requireNonNull(sslContextFactory);
  }

  public ClientFactory() {
    this(com.quorum.tessera.ssl.context.ClientSSLContextFactory.create());
  }

  private static Configuration createUnixServerSocketConfig() {

    return ReflectCallback.execute(
        () -> {
          Class configType = Class.forName("org.glassfish.jersey.client.ClientConfig");
          Class providerClass =
              Class.forName(
                  "com.quorum.tessera.jaxrs.unixsocket.JerseyUnixSocketConnectorProvider");
          Object config = configType.getDeclaredConstructor().newInstance();
          Object provider = providerClass.getDeclaredConstructor().newInstance();
          Method connectorProviderMethod =
              Arrays.stream(configType.getDeclaredMethods())
                  .filter(m -> m.getName().equals("connectorProvider"))
                  .findFirst()
                  .get();

          connectorProviderMethod.invoke(config, provider);

          return Configuration.class.cast(config);
        });
  }

  /**
   * Creates a new client, which may or may not be SSL enabled or a unix socket enabled depending on
   * the configuration.
   *
   * @param config
   * @return
   * @see Client
   */
  public Client buildFrom(final ServerConfig config) {

    final ClientBuilder clientBuilder = ClientBuilder.newBuilder();

    final long pollInterval =
        new IntervalPropertyHelper(config.getProperties()).partyInfoInterval();
    final long timeout = Math.round(Math.ceil(pollInterval * 0.75));
    clientBuilder.connectTimeout(timeout, TimeUnit.MILLISECONDS);
    clientBuilder.readTimeout(timeout, TimeUnit.MILLISECONDS);
    clientBuilder.register(VersionHeaderDecorator.class);

    if (config.isUnixSocket()) {
      Configuration clientConfig = createUnixServerSocketConfig();
      URI unixfile = config.getServerUri();
      return ClientBuilder.newClient(clientConfig).property("unixfile", unixfile);

    } else if (config.isSsl()) {
      final SSLContext sslContext =
          sslContextFactory.from(config.getServerUri().toString(), config.getSslConfig());
      return clientBuilder.sslContext(sslContext).build();
    } else {
      return clientBuilder.build();
    }
  }
}
