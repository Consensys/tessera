package net.consensys.tessera.migration.config;

import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;

import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SslConfigBuilder {

    static SslConfigBuilder create() {
        return new SslConfigBuilder();
    }

    private String sslAuthenticationMode;

    private String clientTrustMode;

    private String serverKeyStore;

    private String clientKeyStore;

    private String clientTlsCertificatePath;

    private String tlsservertrust;

    private String knownServersFilePath;

    private String knownClientFilePath;

    private String serverTlsCertificatePath;

    public SslConfigBuilder withServerTlsCertificatePath(String serverTlsCertificatePath) {
        this.serverTlsCertificatePath = serverTlsCertificatePath;
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

    public SslConfigBuilder withServerKeyStore(String serverKeyStore) {
        this.serverKeyStore = serverKeyStore;
        return this;
    }

    public SslConfigBuilder withTlsServerTrust(String tlsservertrust) {
        this.tlsservertrust = tlsservertrust;
        return this;
    }

    public SslConfigBuilder withClientKeyStore(String clientKeyStore) {
        this.clientKeyStore = clientKeyStore;
        return this;
    }

    public SslConfigBuilder withClientTlsCertificatePath(String clientTlsCertificatePath) {
        this.clientTlsCertificatePath = clientTlsCertificatePath;
        return this;
    }

    public SslConfigBuilder withClientTrustMode(String clientTrustMode) {
        this.clientTrustMode = clientTrustMode;
        return this;
    }

    public SslConfig build() {

        if (Stream.of(serverKeyStore, clientKeyStore, tlsservertrust).allMatch(o -> Objects.isNull(o))) {
            return null;
        }

        SslConfig sslConfig = new SslConfig();
        sslConfig.setGenerateKeyStoreIfNotExisted(false);

        Optional.ofNullable(sslAuthenticationMode)
                .map(String::toUpperCase)
                .map(SslAuthenticationMode::valueOf)
                .ifPresent(sslConfig::setTls);

        Optional.ofNullable(serverKeyStore).map(Paths::get).ifPresent(sslConfig::setServerKeyStore);

        Optional.ofNullable(tlsservertrust)
                .map(String::toUpperCase)
                .map(s -> s.replaceAll("-", "_"))
                .map(SslTrustMode::valueOf)
                .ifPresent(sslConfig::setServerTrustMode);

        Optional.ofNullable(clientKeyStore).map(Paths::get).ifPresent(sslConfig::setClientKeyStore);

        Optional.ofNullable(clientTlsCertificatePath).map(Paths::get).ifPresent(sslConfig::setClientTlsCertificatePath);

        Optional.ofNullable(clientTrustMode)
                .map(String::toUpperCase)
                .map(SslTrustMode::valueOf)
                .ifPresent(sslConfig::setClientTrustMode);

        Optional.ofNullable(knownServersFilePath).map(Paths::get).ifPresent(sslConfig::setKnownServersFile);

        Optional.ofNullable(knownClientFilePath).map(Paths::get).ifPresent(sslConfig::setKnownClientsFile);

        Optional.ofNullable(serverTlsCertificatePath).map(Paths::get).ifPresent(sslConfig::setServerTlsCertificatePath);

        Optional.ofNullable(clientKeyStore).map(Paths::get).ifPresent(sslConfig::setClientKeyStore);

        return sslConfig;
    }
}
