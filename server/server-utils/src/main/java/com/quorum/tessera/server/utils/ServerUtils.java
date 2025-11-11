package com.quorum.tessera.server.utils;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.ssl.context.ServerSSLContextFactory;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class ServerUtils {

  public static Server buildWebServer(ServerConfig serverConfig) {
    Server server = new Server();
    URI uri = serverConfig.getBindingUri();

    if (serverConfig.isUnixSocket()) {

      HttpConfiguration httpConfig = new HttpConfiguration();
      httpConfig.setHttpCompliance(HttpCompliance.LEGACY);
      httpConfig.setUriCompliance(UriCompliance.LEGACY);
      HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);

      UnixDomainServerConnector connector =
          new UnixDomainServerConnector(server, httpConnectionFactory);
      connector.setAcceptQueueSize(128);

      String path = Paths.get(serverConfig.getServerUri()).toAbsolutePath().toString();
      connector.setUnixDomainPath(Paths.get(path));

      server.setConnectors(new Connector[] {connector});

      return server;
    }

    if (serverConfig.isSsl()) {
      HttpConfiguration https = new HttpConfiguration();
      https.setHttpCompliance(HttpCompliance.LEGACY);
      https.setUriCompliance(UriCompliance.LEGACY);
      final SecureRequestCustomizer customizer = new SecureRequestCustomizer();

      Optional.ofNullable(serverConfig.getProperties().get("sniRequired"))
          .map(Boolean::valueOf)
          .ifPresent(customizer::setSniRequired);

      Optional.ofNullable(serverConfig.getProperties().get("sniHostCheck"))
          .map(Boolean::valueOf)
          .ifPresent(customizer::setSniHostCheck);

      https.addCustomizer(customizer);

      SSLContext sslContext =
          ServerSSLContextFactory.create().from(uri.toString(), serverConfig.getSslConfig());

      SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
      sslContextFactory.setSslContext(sslContext);
      sslContextFactory.setNeedClientAuth(serverConfig.getSslConfig().isClientAuth());
      sslContextFactory.setRenegotiationAllowed(false);

      final String[] excludedCipherSuites = serverConfig.getSslConfig().getExcludeCipherSuites();
      if (Objects.nonNull(excludedCipherSuites)) {
        sslContextFactory.addExcludeCipherSuites(excludedCipherSuites);
      }
      ServerConnector connector =
          new ServerConnector(
              server,
              new SslConnectionFactory(sslContextFactory, "http/1.1"),
              new HttpConnectionFactory(https));
      connector.setPort(uri.getPort());
      server.setConnectors(new Connector[] {connector});
      return server;
    }

    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setHttpCompliance(HttpCompliance.LEGACY);
    httpConfig.setUriCompliance(UriCompliance.LEGACY);
    ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
    connector.setPort(uri.getPort());
    server.setConnectors(new Connector[] {connector});

    return server;
  }
}
