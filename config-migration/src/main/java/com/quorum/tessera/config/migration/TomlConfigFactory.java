package com.quorum.tessera.config.migration;

import com.moandjiezana.toml.Toml;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.JdbcConfigFactory;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.builder.SslTrustModeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

public class TomlConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomlConfigFactory.class);

    public ConfigBuilder create(InputStream configData, ArgonOptions options, String... filenames) {
        Objects.requireNonNull(configData, "No config data provided. ");
        if (filenames.length != 0) {
            throw new UnsupportedOperationException("keyConfigData arg is not implemented for TomlConfigFactory");
        }

        Toml toml = new Toml().read(configData);
        toml.toMap().forEach((key, value) -> LOGGER.debug("Found entry in toml file : {} {}", key, value));

        final String urlWithoutPort =
                Optional.ofNullable(toml.getString("url"))
                        .map(
                                url -> {
                                    try {
                                        return new URL(url);
                                    } catch (final MalformedURLException e) {
                                        throw new RuntimeException("Bad server url given: " + e.getMessage());
                                    }
                                })
                        .map(uri -> uri.getProtocol() + "://" + uri.getHost())
                        .orElse(null);

        final Integer port = Optional.ofNullable(toml.getLong("port")).map(Long::intValue).orElse(0);

        final String workdir = toml.getString("workdir", "");
        final String socket = toml.getString("socket");

        final String tls = toml.getString("tls", "off").toUpperCase();

        final List<String> othernodes = toml.getList("othernodes", emptyList());

        final List<String> alwaysSendToKeyPaths = toml.getList("alwayssendto", emptyList());

        final String storage = toml.getString("storage", "memory");

        final List<String> ipwhitelist = toml.getList("ipwhitelist", emptyList());
        final boolean useWhiteList = !ipwhitelist.isEmpty();

        // Server side
        final String tlsservertrust = toml.getString("tlsservertrust", "tofu");
        final Optional<String> tlsserverkey = Optional.ofNullable(toml.getString("tlsserverkey"));
        final Optional<String> tlsservercert = Optional.ofNullable(toml.getString("tlsservercert"));
        final Optional<List<String>> tlsserverchainnames = Optional.of(toml.getList("tlsserverchain", emptyList()));
        final Optional<String> tlsknownclients = Optional.ofNullable(toml.getString("tlsknownclients"));

        // Client side
        final String tlsclienttrust = toml.getString("tlsclienttrust", "tofu");
        final Optional<String> tlsclientkey = Optional.ofNullable(toml.getString("tlsclientkey"));
        final Optional<String> tlsclientcert = Optional.ofNullable(toml.getString("tlsclientcert"));
        final Optional<List<String>> tlsclientchainnames = Optional.of(toml.getList("tlsclientchain", emptyList()));
        final Optional<String> tlsknownservers = Optional.ofNullable(toml.getString("tlsknownservers"));

        ConfigBuilder configBuilder =
                ConfigBuilder.create()
                        .serverPort(port)
                        .serverHostname(urlWithoutPort)
                        .unixSocketFile(socket)
                        .sslAuthenticationMode(SslAuthenticationMode.valueOf(tls))
                        .sslServerTrustMode(SslTrustModeFactory.resolveByLegacyValue(tlsservertrust))
                        .sslClientTrustMode(SslTrustModeFactory.resolveByLegacyValue(tlsclienttrust))
                        .peers(othernodes)
                        .alwaysSendTo(alwaysSendToKeyPaths)
                        .useWhiteList(useWhiteList)
                        .workdir(workdir);

        tlsserverkey.ifPresent(configBuilder::sslServerTlsKeyPath);
        tlsservercert.ifPresent(configBuilder::sslServerTlsCertificatePath);
        tlsserverchainnames.ifPresent(configBuilder::sslServerTrustCertificates);
        tlsknownclients.ifPresent(configBuilder::sslKnownClientsFile);
        tlsclientkey.ifPresent(configBuilder::sslClientTlsKeyPath);
        tlsclientcert.ifPresent(configBuilder::sslClientTlsCertificatePath);
        tlsclientchainnames.ifPresent(configBuilder::sslClientTrustCertificates);
        tlsknownservers.ifPresent(configBuilder::sslKnownServersFile);

        Optional.ofNullable(storage)
                .map(JdbcConfigFactory::fromLegacyStorageString)
                .ifPresent(configBuilder::jdbcConfig);

        return configBuilder;
    }

    KeyDataBuilder createKeyDataBuilder(InputStream configData) {
        final Toml toml = new Toml().read(configData);

        final List<String> publicKeyList = toml.getList("publickeys", emptyList());

        final List<String> privateKeyList = toml.getList("privatekeys", emptyList());

        final String pwd = toml.getString("passwords");

        final String workdir = toml.getString("workdir");

        return KeyDataBuilder.create()
                .withPublicKeys(publicKeyList)
                .withPrivateKeys(privateKeyList)
                .withPrivateKeyPasswordFile(pwd)
                .withWorkingDirectory(workdir);
    }
}
