package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.JdbcConfig;
import com.github.tessera.config.KeyData;
import com.github.tessera.config.Peer;
import com.github.tessera.config.ServerConfig;
import com.github.tessera.config.SslAuthenticationMode;
import com.github.tessera.config.SslConfig;
import com.github.tessera.config.SslTrustMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigBuilder {

    private ConfigBuilder() {
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    private String serverUri;

    private String serverHostname;

    private Integer serverPort;

    private String jdbcUsername;

    private String jdbcPassword;

    private String jdbcUrl;

    private String unixSocketFile;

    private List<String> peers;

    private SslAuthenticationMode sslAuthenticationMode;

    private SslTrustMode sslServerTrustMode;

    private String sslServerKeyStorePath;

    private String sslServerTrustStorePath;

    private String sslClientKeyStorePath;

    private String sslClientKeyStorePassword;
    
    private String sslClientTrustStorePath;
    
    private SslTrustMode sslClientTrustMode;

    private String clientTrustStorePassword;
    
    private String knownClientsFile;
    
    private String knownServersFile;
    
    
    public ConfigBuilder sslServerTrustMode(SslTrustMode sslServerTrustMode) {
        this.sslServerTrustMode = sslServerTrustMode;
        return this;
    }

    public ConfigBuilder sslClientTrustMode(SslTrustMode sslClientTrustMode) {
        this.sslClientTrustMode = sslClientTrustMode;
        return this;
    }

    public ConfigBuilder sslServerKeyStorePath(String sslServerKeyStorePath) {
        this.sslServerKeyStorePath = sslServerKeyStorePath;
        return this;
    }

    public ConfigBuilder sslServerTrustStorePath(String sslServerTrustStorePath) {
        this.sslServerTrustStorePath = sslServerTrustStorePath;
        return this;
    }
    
    public ConfigBuilder unixSocketFile(String unixSocketFile) {
        this.unixSocketFile = unixSocketFile;
        return this;
    }

    public ConfigBuilder serverUri(String serverUri) {
        this.serverUri = serverUri;
        return this;
    }

    public ConfigBuilder serverPort(Integer serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public ConfigBuilder jdbcUsername(String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
        return this;
    }

    public ConfigBuilder jdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
        return this;
    }

    public ConfigBuilder jdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public ConfigBuilder peers(List<String> peers) {
        this.peers = peers;
        return this;
    }

    public ConfigBuilder sslAuthenticationMode(SslAuthenticationMode sslAuthenticationMode) {
        this.sslAuthenticationMode = sslAuthenticationMode;
        return this;
    }

    public Config build() {

        final JdbcConfig jdbcConfig = new JdbcConfig(jdbcUsername, jdbcPassword, jdbcUrl);

        boolean generateKeyStoreIfNotExisted = false;

        String serverKeyStorepassword = null;
        String serverTrustStorePassword = null;
        
        SslConfig sslConfig = new SslConfig(
                sslAuthenticationMode,
                generateKeyStoreIfNotExisted,
                Paths.get(sslServerKeyStorePath),
                serverKeyStorepassword,
                Paths.get(sslServerTrustStorePath),
                serverTrustStorePassword,
                sslServerTrustMode,
                Paths.get(sslClientKeyStorePath),
                sslClientKeyStorePassword,
                Paths.get(sslClientTrustStorePath),
                clientTrustStorePassword,
                sslClientTrustMode,
                Paths.get(knownClientsFile),
                Paths.get(knownServersFile));

        final URI serverURI;
        try {
            serverURI = new URI(serverUri);
        } catch (URISyntaxException ex) {
            throw new ConfigBuilderException(ex);
        }
        ServerConfig serverConfig = new ServerConfig(serverHostname, serverPort, sslConfig);

        List<Peer> peerList = peers.stream()
                .map(Peer::new)
                .collect(Collectors.toList());

        List<KeyData> keys = null;

        Path unixSocketFilePath = Paths.get(unixSocketFile);

        //TODO:
        final boolean useWhitelist = false;

        return new Config(jdbcConfig, serverConfig, peerList, keys, unixSocketFilePath, useWhitelist);
    }

}
