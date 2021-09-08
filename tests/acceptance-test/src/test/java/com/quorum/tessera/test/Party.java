package com.quorum.tessera.test;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.jaxrs.client.ClientFactory;
import db.UncheckedSQLException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Party {

  private final String publicKey;

  private final URI p2pUri;
  private final URI q2tUri;
  private final URI adminUri;
  private final URI thirdPartyUri;

  private final Config config;

  private final String alias;

  private final Path configFilePath;

  public Party(String publicKey, URL configUrl, String alias) {
    this.publicKey = Objects.requireNonNull(publicKey);

    try (InputStream inputStream = configUrl.openStream()) {
      this.configFilePath = Paths.get(configUrl.toURI());
      this.config = JaxbUtil.unmarshal(inputStream, Config.class);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (URISyntaxException ex) {
      throw new RuntimeException(ex);
    }

    ServerConfig p2pServerConfig = config.getP2PServerConfig();
    this.p2pUri = p2pServerConfig.getServerUri();

    ServerConfig q2tServerConfig =
        config.getServerConfigs().stream()
            .filter(sc -> sc.getApp() == AppType.Q2T)
            .findFirst()
            .get();

    this.q2tUri = q2tServerConfig.getServerUri();

    Optional<ServerConfig> adminServerConfig =
        config.getServerConfigs().stream().filter(sc -> sc.getApp() == AppType.ADMIN).findFirst();
    this.adminUri = adminServerConfig.map(ServerConfig::getServerUri).orElse(null);

    Optional<ServerConfig> thirdPartyServerConfig =
        config.getServerConfigs().stream()
            .filter(sc -> sc.getApp() == AppType.THIRD_PARTY)
            .findFirst();
    this.thirdPartyUri = thirdPartyServerConfig.map(ServerConfig::getServerUri).orElse(null);

    this.alias = Objects.requireNonNull(alias);
  }

  public String getPublicKey() {
    return publicKey;
  }

  public URI getP2PUri() {
    return p2pUri;
  }

  public URI getAdminUri() {
    return adminUri;
  }

  public URI getThirdPartyUri() {
    return thirdPartyUri;
  }

  public URI getQ2TUri() {
    return q2tUri;
  }

  public List<String> getAlwaysSendTo() {
    return config.getAlwaysSendTo();
  }

  public Connection getDatabaseConnection() {

    JdbcConfig jdbcConfig = config.getJdbcConfig();

    String url = jdbcConfig.getUrl();
    String username = jdbcConfig.getUsername();
    String password = jdbcConfig.getPassword();
    try {
      return DriverManager.getConnection(url, username, password);
    } catch (SQLException ex) {
      throw new UncheckedSQLException(ex);
    }
  }

  public Client getRestClient() {
    ServerConfig serverConfig =
        config.getServerConfigs().stream().filter(s -> s.getApp() == AppType.Q2T).findAny().get();

    return new ClientFactory().buildFrom(serverConfig);
  }

  public WebTarget getRestClientWebTarget() {
    ServerConfig serverConfig =
        config.getServerConfigs().stream().filter(s -> s.getApp() == AppType.Q2T).findAny().get();
    return getRestClient().target(serverConfig.getServerUri());
  }

  public String getAlias() {
    return alias;
  }

  @Override
  public String toString() {
    return "Party{" + "p2pUri=" + p2pUri + ", q2tUri=" + q2tUri + ", alias=" + alias + '}';
  }

  public Path getConfigFilePath() {
    return configFilePath;
  }

  public Config getConfig() {
    return config;
  }

  public List<Peer> getConfiguredPeers() {
    return Collections.unmodifiableList(config.getPeers());
  }
}
