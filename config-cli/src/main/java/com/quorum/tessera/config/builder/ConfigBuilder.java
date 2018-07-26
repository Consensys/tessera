package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
                    .knownServersFile(Objects.toString(sslConfig.getKnownServersFile()))
                    .sslClientTlsCertificatePath(Objects.toString(sslConfig.getClientTlsCertificatePath()))
                    .sslServerTlsCertificatePath(Objects.toString(sslConfig.getServerTlsCertificatePath()))
                    .sslClientTlsKeyPath(Objects.toString(sslConfig.getClientTlsKeyPath()))
                    .sslServerTlsKeyPath(Objects.toString(sslConfig.getServerTlsKeyPath()));
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

    private String sslServerTlsKeyPath;

    private String sslServerTlsCertificatePath;

    private String sslClientTlsKeyPath;

    private String sslClientTlsCertificatePath;

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

    public ConfigBuilder sslServerTlsKeyPath(String sslServerTlsKeyPath) {
        this.sslServerTlsKeyPath = sslServerTlsKeyPath;
        return this;
    }

    public ConfigBuilder sslServerTlsCertificatePath(String sslServerTlsCertificatePath) {
        this.sslServerTlsCertificatePath = sslServerTlsCertificatePath;
        return this;
    }

    public ConfigBuilder sslClientTlsKeyPath(String sslClientTlsKeyPath) {
        this.sslClientTlsKeyPath = sslClientTlsKeyPath;
        return this;
    }

    public ConfigBuilder sslClientTlsCertificatePath(String sslClientTlsCertificatePath) {
        this.sslClientTlsCertificatePath = sslClientTlsCertificatePath;
        return this;
    }

    public ConfigBuilder keyData(List<KeyData> keyData) {
        this.keyData = keyData;
        return this;
    }

    private static Path toPath(String value) {
        return Optional.ofNullable(value)
            .map(v -> Paths.get(v))
            .orElse(null);
    }

    public Config build() {

        boolean generateKeyStoreIfNotExisted = false;

        SslConfig sslConfig = new SslConfig(
                sslAuthenticationMode,
                generateKeyStoreIfNotExisted,
                toPath(sslServerKeyStorePath),
                sslServerKeyStorePassword,
                toPath(sslServerTrustStorePath),
                sslServerTrustStorePassword,
                sslServerTrustMode,
                toPath(sslClientKeyStorePath),
                sslClientKeyStorePassword,
                toPath(sslClientTrustStorePath),
                sslClientTrustStorePassword,
                sslClientTrustMode,
                toPath(knownClientsFile),
                toPath(knownServersFile),
                sslServerTrustCertificates.stream()
                        .map(ConfigBuilder::toPath)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                sslClientTrustCertificates.stream()
                        .map(ConfigBuilder::toPath)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                toPath(sslServerTlsKeyPath),
                toPath(sslServerTlsCertificatePath),
                toPath(sslClientTlsKeyPath),
                toPath(sslClientTlsCertificatePath)
        );

        final ServerConfig serverConfig = new ServerConfig(serverHostname, serverPort, sslConfig, null);

        final List<Peer> peerList = peers.stream()
                .map(Peer::new)
                .collect(Collectors.toList());

        Path unixSocketFilePath = Paths.get(unixSocketFile);

        //TODO:
        final boolean useWhitelist = false;

        return new Config(jdbcConfig, serverConfig, peerList, keyData, unixSocketFilePath, useWhitelist);
    }

}
