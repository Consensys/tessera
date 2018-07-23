package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.JdbcConfig;
import com.github.tessera.config.KeyData;
import com.github.tessera.config.Peer;
import com.github.tessera.config.ServerConfig;
import com.github.tessera.config.SslAuthenticationMode;
import com.github.tessera.config.SslConfig;
import com.github.tessera.config.SslTrustMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigBuilder {

    private ConfigBuilder() {
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }


    public static ConfigBuilder from(Config config) {

        final ConfigBuilder configBuilder = ConfigBuilder.create();
        configBuilder.unixSocketFile(Objects.toString(config.getUnixSocketFile()));

        configBuilder.jdbcUrl(config.getJdbcConfig().getUrl())
                .jdbcUsername(config.getJdbcConfig().getUsername())
                .jdbcPassword(config.getJdbcConfig().getPassword())
                .peers(config.getPeers()
                        .stream()
                        .map(Peer::getUrl)
                        .collect(Collectors.toList()))
                .serverHostname(config.getServerConfig().getHostName())
                .serverPort(config.getServerConfig().getPort());

        final SslConfig sslConfig = config.getServerConfig().getSslConfig();
        
        if (Objects.nonNull(sslConfig)) {
            configBuilder.sslAuthenticationMode(sslConfig.getTls())
                    
                    .sslClientTrustMode(sslConfig.getClientTrustMode())
                    .sslClientKeyStorePath(Objects.toString(sslConfig.getClientKeyStore()))
                    .sslClientKeyStorePassword(sslConfig.getClientKeyStorePassword())
                    .sslClientTrustStorePath(Objects.toString(sslConfig.getClientTrustStore()))
                    .sslClientTrustStorePassword(sslConfig.getClientTrustStorePassword())
                    
                    .sslServerTrustMode(sslConfig.getServerTrustMode())
                    .sslServerKeyStorePath(Objects.toString(sslConfig.getServerKeyStore()))
                    .sslServerKeyStorePassword(sslConfig.getServerKeyStorePassword())
                    .sslServerTrustStorePath(Objects.toString(sslConfig.getServerTrustStore()))
                    .sslServerTrustStorePassword(sslConfig.getServerTrustStorePassword())
                    .knownClientsFile(Objects.toString(sslConfig.getKnownClientsFile()))
                    .knownServersFile(Objects.toString(sslConfig.getKnownServersFile()));

        }

        
        return configBuilder;

    }

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
    
    private String sslServerTrustStorePassword;
    
    private String sslServerKeyStorePassword;

    private String sslServerTrustStorePath;

    private String sslClientKeyStorePath;

    private String sslClientKeyStorePassword;

    private String sslClientTrustStorePassword;

    private String sslClientTrustStorePath;

    private SslTrustMode sslClientTrustMode;

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

    public ConfigBuilder sslServerTrustStorePassword(String sslServerTrustStorePassword) {
        this.sslServerTrustStorePassword = sslServerTrustStorePassword;
        return this;
    } 
    
    public ConfigBuilder sslServerKeyStorePassword(String sslServerKeyStorePassword) {
        this.sslServerKeyStorePassword = sslServerKeyStorePassword;
        return this;
    }

    public ConfigBuilder sslServerTrustStorePath(String sslServerTrustStorePath) {
        this.sslServerTrustStorePath = sslServerTrustStorePath;
        return this;
    }

    public ConfigBuilder sslClientTrustStorePassword(String sslClientTrustStorePassword) {
        this.sslClientTrustStorePassword = sslClientTrustStorePassword;
        return this;
    }

    public ConfigBuilder unixSocketFile(String unixSocketFile) {
        this.unixSocketFile = unixSocketFile;
        return this;
    }

    public ConfigBuilder serverHostname(String serverHostname) {
        this.serverHostname = serverHostname;
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

    public ConfigBuilder knownClientsFile(String knownClientsFile) {
        this.knownClientsFile = knownClientsFile;
        return this;
    }

    public ConfigBuilder knownServersFile(String knownServersFile) {
        this.knownServersFile = knownServersFile;
        return this;
    }

    public ConfigBuilder sslAuthenticationMode(SslAuthenticationMode sslAuthenticationMode) {
        this.sslAuthenticationMode = sslAuthenticationMode;
        return this;
    }

    public ConfigBuilder sslClientKeyStorePath(String sslClientKeyStorePath) {
        this.sslClientKeyStorePath = sslClientKeyStorePath;
        return this;
    }

    public ConfigBuilder sslClientTrustStorePath(String sslClientTrustStorePath) {
        this.sslClientTrustStorePath = sslClientTrustStorePath;
        return this;
    }

    public ConfigBuilder sslClientKeyStorePassword(String sslClientKeyStorePassword) {
        this.sslClientKeyStorePassword = sslClientKeyStorePassword;
        return this;
    }

    public Config build() {

        final JdbcConfig jdbcConfig = new JdbcConfig(jdbcUsername, jdbcPassword, jdbcUrl);

        boolean generateKeyStoreIfNotExisted = false;


        SslConfig sslConfig = new SslConfig(
                sslAuthenticationMode,
                generateKeyStoreIfNotExisted,
                Paths.get(sslServerKeyStorePath),
                sslServerKeyStorePassword,
                Paths.get(sslServerTrustStorePath),
                sslServerTrustStorePassword,
                sslServerTrustMode,
                Paths.get(sslClientKeyStorePath),
                sslClientKeyStorePassword,
                Paths.get(sslClientTrustStorePath),
                sslClientTrustStorePassword,
                sslClientTrustMode,
                Paths.get(knownClientsFile),
                Paths.get(knownServersFile));

        final ServerConfig serverConfig = new ServerConfig(serverHostname, serverPort, sslConfig);

        final List<Peer> peerList = peers.stream()
                .map(Peer::new)
                .collect(Collectors.toList());

        final List<KeyData> keys = Collections.EMPTY_LIST;

        Path unixSocketFilePath = Paths.get(unixSocketFile);

        //TODO:
        final boolean useWhitelist = false;

        return new Config(jdbcConfig, serverConfig, peerList, keys, unixSocketFilePath, useWhitelist);
    }

}
