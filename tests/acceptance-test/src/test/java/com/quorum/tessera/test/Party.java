package com.quorum.tessera.test;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.Peer;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.JaxbUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.UriBuilder;
import java.nio.file.Path;
import java.util.Collections;

public class Party {

    private final String publicKey;

    private final URI uri;

    private final Config config;

    private final String alias;
    
    private final Path configFilePath;
    
    public Party(String publicKey, URL configUrl,String alias) {
        this.publicKey = Objects.requireNonNull(publicKey);

        try (InputStream inputStream = configUrl.openStream()) {
            this.configFilePath = Paths.get(configUrl.toURI());
            this.config = JaxbUtil.unmarshal(inputStream, Config.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        
        ServerConfig serverConfig = config.getServerConfig();
        this.uri = UriBuilder.fromUri(serverConfig.getHostName())
            .port(serverConfig.getPort())
            .build();
        
        this.alias = Objects.requireNonNull(alias);

    }

    public String getPublicKey() {
        return publicKey;
    }

    public URI getUri() {
        return uri;
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
    
    public String getGprcHostName() {
        return config.getServerConfig()
                .getGrpcUri().getHost();
    
    }
    
    public Integer getGrpcPort() {
        return config.getServerConfig()
                .getGrpcPort();
    }
    
    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "Party{" + "uri=" + uri + ", alias=" + alias + '}';
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
