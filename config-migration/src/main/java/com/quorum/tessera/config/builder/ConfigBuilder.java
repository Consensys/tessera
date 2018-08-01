package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;
import com.quorum.tessera.nacl.Key;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;

public class ConfigBuilder {

    private ConfigBuilder() {
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    public static ConfigBuilder from(Config config) {

        final ConfigBuilder configBuilder = ConfigBuilder.create();
        configBuilder.unixSocketFile(config.getUnixSocketFile());

        List<String> peers = Stream.of(config)
            .filter(c -> c.getPeers() != null)
            .map(Config::getPeers)
            .flatMap(List::stream)
            .map(Peer::getUrl)
            .collect(Collectors.toList());

        configBuilder.jdbcConfig(config.getJdbcConfig())
                .peers(peers)
                .serverHostname(config.getServerConfig().getHostName())
                .serverPort(config.getServerConfig().getPort())
                .useWhiteList(config.isUseWhiteList());

        final SslConfig sslConfig = config.getServerConfig().getSslConfig();


        configBuilder.sslAuthenticationMode(sslConfig.getTls())
                .sslClientTrustMode(sslConfig.getClientTrustMode())
                .sslClientKeyStorePath(Objects.toString(sslConfig.getClientKeyStore(), null))
                .sslClientKeyStorePassword(sslConfig.getClientKeyStorePassword())
                .sslClientTrustStorePath(Objects.toString(sslConfig.getClientTrustStore(), null))
                .sslClientTrustStorePassword(sslConfig.getClientTrustStorePassword())
                .sslClientTlsKeyPath(Objects.toString(sslConfig.getClientTlsKeyPath(),null))
                .sslClientTlsCertificatePath(
                    Objects.toString(sslConfig.getClientTlsCertificatePath(),null)
                )
                .sslClientTrustCertificates(Objects.isNull(sslConfig.getClientTrustCertificates()) ?
                    EMPTY_LIST :
                    sslConfig.getClientTrustCertificates().stream().map(Path::toString).collect(Collectors.toList()))
                .sslKnownServersFile(Objects.toString(sslConfig.getKnownServersFile(), null))


                .sslServerTrustMode(sslConfig.getServerTrustMode())
                .sslServerKeyStorePath(Objects.toString(sslConfig.getServerKeyStore(), null))
                .sslServerKeyStorePassword(sslConfig.getServerKeyStorePassword())
                .sslServerTrustStorePath(Objects.toString(sslConfig.getServerTrustStore(), null))
                .sslServerTrustStorePassword(sslConfig.getServerTrustStorePassword())
                .sslServerTlsKeyPath(Objects.toString(sslConfig.getServerTlsKeyPath(),null))
                .sslServerTlsCertificatePath(Objects.toString(sslConfig.getServerTlsCertificatePath(),null))
                .sslServerTrustCertificates(Objects.isNull(sslConfig.getServerTrustCertificates()) ?
                    EMPTY_LIST :
                    sslConfig.getServerTrustCertificates().stream().map(Path::toString).collect(Collectors.toList())
                )
                .sslKnownClientsFile(Objects.toString(sslConfig.getKnownClientsFile(), null))

                .keyData(config.getKeys())

                .alwaysSendTo(Stream.of(config)
                    .filter(c -> c.getFowardingList() != null)
                    .map(Config::getFowardingList)
                    .flatMap(List::stream)
                    .map(Key::toString)
                    .collect(Collectors.toList()));

        return configBuilder;

    }

    private String serverHostname;

    private Integer serverPort;

    private JdbcConfig jdbcConfig;

    private Path unixSocketFile;

    private List<String> peers;

    private List<String> alwaysSendTo;

    private KeyConfiguration keyData;

    private SslAuthenticationMode sslAuthenticationMode;

    private SslTrustMode sslServerTrustMode;

    private String sslServerKeyStorePath;

    private String sslServerTrustStorePassword;

    private String sslServerKeyStorePassword;

    private String sslServerTrustStorePath;

    private List<String> sslServerTrustCertificates = emptyList();

    private String sslClientKeyStorePath;

    private String sslClientKeyStorePassword;

    private String sslClientTrustStorePassword;

    private String sslClientTrustStorePath;

    private List<String> sslClientTrustCertificates = emptyList();

    private SslTrustMode sslClientTrustMode;

    private String sslKnownClientsFile;

    private String sslKnownServersFile;

    private String sslServerTlsKeyPath;

    private String sslServerTlsCertificatePath;

    private String sslClientTlsKeyPath;

    private String sslClientTlsCertificatePath;

    private boolean useWhiteList;

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

    public ConfigBuilder unixSocketFile(Path unixSocketFile) {
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

    public ConfigBuilder alwaysSendTo(List<String> alwaysSendTo) {
        this.alwaysSendTo = alwaysSendTo;
        return this;
    }

    public ConfigBuilder sslKnownClientsFile(String knownClientsFile) {
        this.sslKnownClientsFile = knownClientsFile;
        return this;
    }

    public ConfigBuilder sslKnownServersFile(String knownServersFile) {
        this.sslKnownServersFile = knownServersFile;
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

    public ConfigBuilder keyData(KeyConfiguration keyData) {
        this.keyData = keyData;
        return this;
    }

    public ConfigBuilder useWhiteList(boolean useWhiteList) {
        this.useWhiteList = useWhiteList;
        return this;
    }

    static Path toPath(String value) {
        return Optional.ofNullable(value)
                .map(v -> Paths.get(v))
                .orElse(null);
    }

    public Config build() {

        boolean generateKeyStoreIfNotExisted = true;

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
                toPath(sslKnownClientsFile),
                toPath(sslKnownServersFile),
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

        final List<Peer> peerList = peers
            .stream()
                .map(Peer::new)
                .collect(Collectors.toList());

        final List<Key> forwardingKeys = alwaysSendTo
            .stream()
            .map(Base64.getDecoder()::decode)
            .map(Key::new)
            .collect(Collectors.toList());

        final boolean useWhitelist = useWhiteList;

        return new Config(jdbcConfig, serverConfig, peerList, keyData, forwardingKeys, unixSocketFile, useWhitelist);
    }

}
