package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;

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

        configBuilder.jdbcConfig(config.getJdbcConfig())
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

        final InfluxConfig influxConfig = config.getServerConfig().getInfluxConfig();

        if(Objects.nonNull(influxConfig)) {
            configBuilder.influxHostName(influxConfig.getHostName())
                        .influxPort(influxConfig.getPort())
                        .pushIntervalInSecs(influxConfig.getPushIntervalInSecs())
                        .influxDbName(influxConfig.getDbName());
        }

        return configBuilder;

    }

    private String serverHostname;

    private Integer serverPort;

    private JdbcConfig jdbcConfig;

    private String unixSocketFile;

    private List<String> peers;

    private List<KeyData> keyData;

    private SslAuthenticationMode sslAuthenticationMode;

    private SslTrustMode sslServerTrustMode;

    private String sslServerKeyStorePath;

    private String sslServerTrustStorePassword;

    private String sslServerKeyStorePassword;

    private String sslServerTrustStorePath;

    private List<String> sslServerTrustCertificates = Collections.emptyList();

    private String sslClientKeyStorePath;

    private String sslClientKeyStorePassword;

    private String sslClientTrustStorePassword;

    private String sslClientTrustStorePath;

    private List<String> sslClientTrustCertificates = Collections.emptyList();

    private SslTrustMode sslClientTrustMode;

    private String knownClientsFile;

    private String knownServersFile;

    private String influxHostName;

    private int influxPort;

    private Long pushIntervalInSecs;

    private String influxDbName;

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

    public ConfigBuilder sslServerTrustCertificates(List<String> sslServerTrustCertificates) {
        this.sslServerTrustCertificates = sslServerTrustCertificates;
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

    public ConfigBuilder jdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
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

    public ConfigBuilder sslClientTrustCertificates(List<String> sslClientTrustCertificates) {
        this.sslClientTrustCertificates = sslClientTrustCertificates;
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

    public ConfigBuilder keyData(List<KeyData> keyData) {
        this.keyData = keyData;
        return this;
    }

    public ConfigBuilder influxHostName(String influxHostName) {
        this.influxHostName = influxHostName;
        return this;
    }

    public ConfigBuilder influxPort(int influxPort) {
        this.influxPort = influxPort;
        return this;
    }

    public ConfigBuilder pushIntervalInSecs(Long pushIntervalInSecs) {
        this.pushIntervalInSecs = pushIntervalInSecs;
        return this;
    }

    public ConfigBuilder influxDbName(String influxDbName) {
        this.influxDbName= influxDbName;
        return this;
    }

    public Config build() {

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
                Paths.get(knownServersFile),
                sslServerTrustCertificates.stream()
                        .map(v -> Paths.get(v))
                        .collect(Collectors.toList()),
                sslClientTrustCertificates.stream()
                        .map(v -> Paths.get(v))
                        .collect(Collectors.toList()));

        InfluxConfig influxConfig;
        if(influxHostName != "") {
            influxConfig = new InfluxConfig(
                influxHostName,
                influxPort,
                pushIntervalInSecs,
                influxDbName
            );
        } else {
            influxConfig = null;
        }

        final ServerConfig serverConfig = new ServerConfig(serverHostname, serverPort, sslConfig, influxConfig);

        final List<Peer> peerList = peers.stream()
                .map(Peer::new)
                .collect(Collectors.toList());

        Path unixSocketFilePath = Paths.get(unixSocketFile);

        //TODO:
        final boolean useWhitelist = false;

        return new Config(jdbcConfig, serverConfig, peerList, keyData, unixSocketFilePath, useWhitelist);
    }

}
