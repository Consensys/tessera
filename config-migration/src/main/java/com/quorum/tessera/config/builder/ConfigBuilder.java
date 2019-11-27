package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.quorum.tessera.config.AppType.P2P;
import static com.quorum.tessera.config.AppType.Q2T;
import static com.quorum.tessera.config.CommunicationType.REST;
import static java.util.Collections.emptyList;

public class ConfigBuilder {

    private ConfigBuilder() {}

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    private String serverHostname;

    private Integer serverPort = 0;

    private JdbcConfig jdbcConfig;

    private String unixSocketFile;

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

    private String workDir;

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

    public ConfigBuilder workdir(String workDir) {
        this.workDir = workDir;
        return this;
    }

    static Path toPath(final String workDir, final String value) {
        if (workDir != null && value != null) {
            return Paths.get(workDir, value);
        } else if (value != null) {
            return Paths.get(value);
        }
        return null;
    }

    public Config build() {

        boolean generateKeyStoreIfNotExisted = false;

        SslConfig sslConfig =
                new SslConfig(
                        sslAuthenticationMode,
                        generateKeyStoreIfNotExisted,
                        toPath(workDir, sslServerKeyStorePath),
                        sslServerKeyStorePassword,
                        toPath(workDir, sslServerTrustStorePath),
                        sslServerTrustStorePassword,
                        sslServerTrustMode,
                        toPath(workDir, sslClientKeyStorePath),
                        sslClientKeyStorePassword,
                        toPath(workDir, sslClientTrustStorePath),
                        sslClientTrustStorePassword,
                        sslClientTrustMode,
                        toPath(workDir, sslKnownClientsFile),
                        toPath(workDir, sslKnownServersFile),
                        sslServerTrustCertificates.stream()
                                .filter(Objects::nonNull)
                                .map(v -> toPath(workDir, v))
                                .collect(Collectors.toList()),
                        sslClientTrustCertificates.stream()
                                .filter(Objects::nonNull)
                                .map(v -> toPath(workDir, v))
                                .collect(Collectors.toList()),
                        toPath(workDir, sslServerTlsKeyPath),
                        toPath(workDir, sslServerTlsCertificatePath),
                        toPath(workDir, sslClientTlsKeyPath),
                        toPath(workDir, sslClientTlsCertificatePath),
                        null);

        final String unixPath =
                Optional.ofNullable(toPath(workDir, unixSocketFile))
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .map("unix:"::concat)
                        .orElse(null);
        final ServerConfig q2tConfig = new ServerConfig(Q2T, true, unixPath, REST, null, null, null);

        final String address = (serverHostname == null) ? null : serverHostname + ":" + serverPort;
        final ServerConfig p2pConfig = new ServerConfig(P2P, true, address, REST, sslConfig, null, address);

        final List<Peer> peerList;
        if (peers != null) {
            peerList = peers.stream().map(Peer::new).collect(Collectors.toList());
        } else {
            peerList = null;
        }

        final List<String> forwardingKeys = new ArrayList<>();
        if (alwaysSendTo != null) {
            for (String keyPath : alwaysSendTo) {
                try {
                    List<String> keysFromFile = Files.readAllLines(toPath(workDir, keyPath));
                    forwardingKeys.addAll(keysFromFile);
                } catch (IOException e) {
                    System.err.println("Error reading alwayssendto file: " + e.getMessage());
                }
            }
        }

        final Config config = new Config();
        config.setServerConfigs(Arrays.asList(q2tConfig, p2pConfig));
        config.setEncryptor(
                new EncryptorConfig() {
                    {
                        setType(EncryptorType.NACL);
                    }
                });

        config.setJdbcConfig(jdbcConfig);
        config.setPeers(peerList);
        config.setAlwaysSendTo(forwardingKeys);
        config.setUseWhiteList(useWhiteList);
        config.setKeys(keyData);
        config.setDisablePeerDiscovery(false);
        return config;
    }
}
