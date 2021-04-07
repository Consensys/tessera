package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslConfigType;
import com.quorum.tessera.config.SslTrustMode;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SslConfigBuilder {

    static SslConfigBuilder create() {
        return new SslConfigBuilder();
    }

    private String sslAuthenticationMode;

    private String serverTlsKey;

    private String clientTlsKey;

    private String serverTlsCertificatePath;

    private String clientTlsCertificatePath;

    private List<String> clientTrustCertificates;

    private List<String> serverTrustCertificates;

    private String tlsServerTrustMode;

    private String tlsClientTrustMode;

    private String knownServersFilePath;

    private String knownClientFilePath;

    private String sslConfigType;

    public SslConfigBuilder withServerTlsCertificatePath(String serverTlsCertificatePath) {
        this.serverTlsCertificatePath = serverTlsCertificatePath;
        return this;
    }

    public SslConfigBuilder withServerTlsTrustCertChain(List<String> serverTlsTrustCertChain) {
        this.serverTrustCertificates = serverTlsTrustCertChain;
        return this;
    }

    public SslConfigBuilder withClientTlsTrustCertChain(List<String> clientTlsTrustCertChain) {
        this.clientTrustCertificates = clientTlsTrustCertChain;
        return this;
    }

    public SslConfigBuilder withKnownClientFilePath(String knownClientFilePath) {
        this.knownClientFilePath = knownClientFilePath;
        return this;
    }

    public SslConfigBuilder withKnownServersFilePath(String knownServersFilePath) {
        this.knownServersFilePath = knownServersFilePath;
        return this;
    }

    public SslConfigBuilder withSslAuthenticationMode(String sslAuthenticationMode) {
        this.sslAuthenticationMode = sslAuthenticationMode;
        return this;
    }

    public SslConfigBuilder withServerTlsKey(String serverTlsKey) {
        this.serverTlsKey = serverTlsKey;
        return this;
    }

    public SslConfigBuilder withTlsServerTrustMode(String tlsServerTrustMode) {
        this.tlsServerTrustMode = tlsServerTrustMode;
        return this;
    }

    public SslConfigBuilder withTlsClientTrustMode(String tlsClientTrustMode) {
        this.tlsClientTrustMode = tlsClientTrustMode;
        return this;
    }

    public SslConfigBuilder withClientTlsKey(String clientTlsKey) {
        this.clientTlsKey = clientTlsKey;
        return this;
    }

    public SslConfigBuilder withClientTlsCertificatePath(String clientTlsCertificatePath) {
        this.clientTlsCertificatePath = clientTlsCertificatePath;
        return this;
    }

    public SslConfigBuilder withSslConfigType(String sslConfigType) {
        this.sslConfigType = sslConfigType;
        return this;
    }

    public SslConfig build() {

        if (Stream.of(serverTlsKey, clientTlsKey, tlsServerTrustMode, tlsClientTrustMode).allMatch(o -> Objects.isNull(o))) {
            return null;
        }

        SslConfig sslConfig = new SslConfig();
        sslConfig.setGenerateKeyStoreIfNotExisted(false);

        Optional.ofNullable(sslAuthenticationMode)
                .map(String::toUpperCase)
                .map(SslAuthenticationMode::valueOf)
                .ifPresent(sslConfig::setTls);

        Optional.ofNullable(serverTlsKey).map(Paths::get).ifPresent(sslConfig::setServerTlsKeyPath);

        Optional.ofNullable(serverTlsCertificatePath).map(Paths::get).ifPresent(sslConfig::setServerTlsCertificatePath);

        Optional.ofNullable(tlsServerTrustMode)
                .map(String::toUpperCase)
                .map(s -> s.replaceAll("-", "_"))
                .map(SslTrustMode::valueOf)
                .ifPresent(sslConfig::setServerTrustMode);

        Optional.ofNullable(clientTlsKey).map(Paths::get).ifPresent(sslConfig::setClientTlsKeyPath);

        Optional.ofNullable(clientTlsCertificatePath).map(Paths::get).ifPresent(sslConfig::setClientTlsCertificatePath);

        Optional.ofNullable(tlsClientTrustMode)
                .map(String::toUpperCase)
                .map(s -> s.replaceAll("-", "_"))
                .map(SslTrustMode::valueOf)
                .ifPresent(sslConfig::setClientTrustMode);

        Optional.ofNullable(knownServersFilePath).map(Paths::get).ifPresent(sslConfig::setKnownServersFile);

        Optional.ofNullable(knownClientFilePath).map(Paths::get).ifPresent(sslConfig::setKnownClientsFile);

        sslConfig.setClientTrustCertificates(
            Stream.ofNullable(clientTrustCertificates).flatMap(Collection::stream).map(Paths::get).collect(Collectors.toList())
        );

        sslConfig.setServerTrustCertificates(
            Stream.ofNullable(serverTrustCertificates).flatMap(Collection::stream).map(Paths::get).collect(Collectors.toList())
        );

        Optional.ofNullable(sslConfigType).map(SslConfigType::valueOf).ifPresent(sslConfig::setSslConfigType);

        return sslConfig;
    }
}
