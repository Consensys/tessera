package com.quorum.tessera.config.migration;

import com.moandjiezana.toml.Toml;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.JdbcConfigFactory;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.builder.SslTrustModeFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.IOCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TomlConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomlConfigFactory.class);

    public ConfigBuilder create(InputStream configData, ArgonOptions options, String... filenames) {
        Objects.requireNonNull(configData, "No config data provided. ");
        if (filenames.length != 0) {
            throw new UnsupportedOperationException("keyConfigData arg is not implemented for TomlConfigFactory");
        }

        Toml toml = new Toml().read(configData);
        toml.toMap().entrySet().stream().forEach(entry -> {
            LOGGER.debug("Found entry in toml file : {} {}", entry.getKey(), entry.getValue());
        });

        final String urlWithoutPort = Optional
            .ofNullable(toml.getString("url"))
            .map(url -> {
                try {
                    return new URL(url);
                } catch (final MalformedURLException e) {
                    throw new RuntimeException("Bad server url given: " + e.getMessage());
                }
            }).map(uri -> uri.getProtocol() + "://" + uri.getHost())
            .orElse(null);

        final Integer port = Optional.ofNullable(toml.getLong("port"))
                                                    .map(Long::intValue)
                                                    .orElse(null);

        final String workdir = toml.getString("workdir", "");
        final String socket = toml.getString("socket");

        final String tls = toml.getString("tls", "off").toUpperCase();

        final List<String> othernodes = toml.getList("othernodes", Collections.emptyList());

        final List<String> alwaysSendToKeyPaths = toml.getList("alwayssendto", Collections.emptyList());

        final String storage = toml.getString("storage", "memory");

        final List<String> ipwhitelist = toml.getList("ipwhitelist", Collections.EMPTY_LIST);
        final boolean useWhiteList = !ipwhitelist.isEmpty();

        //Server side
        final String tlsservertrust = toml.getString("tlsservertrust", "tofu");
        final Optional<String> tlsserverkey = Optional.ofNullable(toml.getString("tlsserverkey"));
        final Optional<String> tlsservercert = Optional.ofNullable(toml.getString("tlsservercert"));
        final Optional<List<String>> tlsserverchainnames = Optional.ofNullable(toml.getList("tlsserverchain", Collections.emptyList()));
        final Optional<String> tlsknownclients = Optional.ofNullable(toml.getString("tlsknownclients"));

        //Client side
        final String tlsclienttrust = toml.getString("tlsclienttrust", "tofu");
        final Optional<String> tlsclientkey = Optional.ofNullable(toml.getString("tlsclientkey"));
        final Optional<String> tlsclientcert = Optional.ofNullable(toml.getString("tlsclientcert"));
        final Optional<List<String>> tlsclientchainnames = Optional.ofNullable(toml.getList("tlsclientchain", Collections.emptyList()));
        final Optional<String> tlsknownservers = Optional.ofNullable(toml.getString("tlsknownservers"));

        ConfigBuilder configBuilder = ConfigBuilder.create()
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
                .map(JdbcConfigFactory::fromLegacyStorageString).ifPresent(configBuilder::jdbcConfig);

        return configBuilder;
    }

    public KeyDataBuilder createKeyDataBuilder(InputStream configData) {
        Toml toml = new Toml().read(configData);

        final List<String> publicKeyList = toml.getList("publickeys", Collections.emptyList());

        final List<String> privateKeyList = toml.getList("privatekeys", Collections.emptyList());

        final String pwd = toml.getString("passwords");

        final String workdir = toml.getString("workdir");

        KeyDataBuilder keyDataBuilder = KeyDataBuilder.create();
        if(!publicKeyList.isEmpty() || !privateKeyList.isEmpty()) {
            keyDataBuilder.withPublicKeys(publicKeyList)
                          .withPrivateKeys(privateKeyList)
                          .withPrivateKeyPasswordFile(pwd)
                          .withWorkingDirectory(workdir);
        }

        return keyDataBuilder;
    }

    static List<KeyDataConfig> createPrivateKeyData(List<String> privateKeys, List<String> privateKeyPasswords) {

        //Populate null values assume that they arent private
        List<String> passwordList = new ArrayList<>(privateKeyPasswords);
        for (int i = privateKeyPasswords.size() - 1; i < privateKeys.size(); i++) {
            passwordList.add(null);
        }

        List<JsonObject> privateKeyJson = privateKeys
                .stream()
                .map(Paths::get)
                .map(path -> IOCallback.execute(() -> Files.newInputStream(path)))
                .map(Json::createReader)
                .map(JsonReader::readObject)
                .collect(Collectors.toList());

        List<KeyDataConfig> privateKeyData = IntStream
                .range(0, privateKeyJson.size())
                .mapToObj(i -> {

                    final String password = passwordList.get(i);
                    final JsonObject keyDatC = Json.createObjectBuilder(privateKeyJson.get(i)).build();

                    final JsonObject dataNode = keyDatC.getJsonObject("data");
                    final JsonObjectBuilder ammendedDataNode = Json.createObjectBuilder(dataNode);

                    boolean isLocked = Objects.equals(keyDatC.getString("type"), "argon2sbox");
                    if (isLocked) {
                        ammendedDataNode.add("password", Objects.requireNonNull(password, "Password is required."));
                    }

                    return Json.createObjectBuilder(keyDatC)
                            .remove("data")
                            .add("data", ammendedDataNode)
                            .build();
                })
                .map(JsonObject::toString)
                .map(String::getBytes)
                .map(ByteArrayInputStream::new)
                .map(inputStream -> JaxbUtil.unmarshal(inputStream, KeyDataConfig.class))
                .collect(Collectors.toList());

        return Collections.unmodifiableList(privateKeyData);
    }

}
