package com.quorum.tessera.config.migration;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.JdbcConfigFactory;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.io.FilesDelegate;
import com.quorum.tessera.io.IOCallback;
import com.moandjiezana.toml.Toml;
import com.quorum.tessera.config.builder.SslTrustModeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TomlConfigFactory implements ConfigFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomlConfigFactory.class);

    private final FilesDelegate filesDelegate;

    public TomlConfigFactory() {
        this(FilesDelegate.create());
    }

    public TomlConfigFactory(FilesDelegate filesDelegate) {
        this.filesDelegate = Objects.requireNonNull(filesDelegate);
    }

    @Override
    public Config create(InputStream configData, InputStream... keyConfigData) {
        Objects.requireNonNull(configData, "No config data provided. ");
        if (keyConfigData.length != 0) {
            throw new UnsupportedOperationException("keyConfigData arg is not implemented for TomlConfigFactory");
        }

        Toml toml = new Toml().read(configData);
        toml.toMap().entrySet().stream().forEach(entry -> {
            LOGGER.debug("Found entry in toml file : {} {}", entry.getKey(), entry.getValue());
        });

        final String url = toml.getString("url");

        final Integer port = Optional.ofNullable(toml.getLong("port"))
                                    .map(Long::intValue)
                                    .orElse(null);

        final String workdir = toml.getString("workdir", "");
        final String socket = toml.getString("socket", "temp-socket.ipc");

        final Path unixSocketFile = Paths.get(workdir, socket);

        final String tls = toml.getString("tls", "strict").toUpperCase();

        final List<String> othernodes = toml.getList("othernodes", Collections.EMPTY_LIST);

        final List<String> publicKeyList = toml.getList("publickeys", Collections.EMPTY_LIST);

        final List<String> privateKeyList = toml.getList("privatekeys", Collections.EMPTY_LIST);

        final Optional<String> privateKeyPasswordFile = Optional.ofNullable(toml.getString("passwords"));
        final Path privateKeyPasswordPath;
        if(privateKeyPasswordFile.isPresent()) {
            privateKeyPasswordPath = Paths.get(workdir, privateKeyPasswordFile.get());
        } else {
            privateKeyPasswordPath = null;
        }

        KeyConfiguration keyData;
        if(!publicKeyList.isEmpty() && !privateKeyList.isEmpty()) {
            keyData = KeyDataBuilder.create()
                .withPublicKeys(publicKeyList)
                .withPrivateKeys(privateKeyList)
                .withPrivateKeyPasswordFile(privateKeyPasswordPath)
                .build(workdir);
        } else {
            keyData = new KeyConfiguration(null, null, null);
        }

        final List<String> alwaysSendToKeyPaths = toml.getList("alwayssendto", Collections.EMPTY_LIST);

        String storage = toml.getString("storage", "memory");

        final List<String> ipwhitelist = toml.getList("ipwhitelist", Collections.EMPTY_LIST);
        final boolean useWhiteList = ipwhitelist.isEmpty() ? false : true;

        //Server side
        final String tlsservertrust = toml.getString("tlsservertrust", "tofu");
        final Path tlsserverkey = Paths.get(workdir, toml.getString("tlsserverkey", "tls-server-key.pem"));
        final Path tlsservercert = Paths.get(workdir, toml.getString("tlsservercert", "tls-server-cert.pem"));
        final List<String> tlsserverchainnames = toml.getList("tlsserverchain", Collections.EMPTY_LIST);
        List<Path> tlsserverchain = new ArrayList<>();
        for(String name : tlsserverchainnames) {
            tlsserverchain.add(Paths.get(workdir, name));
        }
        final Path tlsknownclients = Paths.get(workdir, toml.getString("tlsknownclients", "tls-known-clients"));

        //Client side
        final String tlsclienttrust = toml.getString("tlsclienttrust", "tofu");
        final Path tlsclientkey = Paths.get(workdir, toml.getString("tlsclientkey", "tls-client-key.pem"));
        final Path tlsclientcert = Paths.get(workdir, toml.getString("tlsclientcert", "tls-client-cert.pem"));
        final List<String> tlsclientchainnames = toml.getList("tlsclientchain", Collections.EMPTY_LIST);
        List<Path> tlsclientchain = new ArrayList<>();
        for(String name : tlsclientchainnames) {
            tlsclientchain.add(Paths.get(workdir, name));
        }
        final Path tlsknownservers = Paths.get(workdir, toml.getString("tlsknownservers", "tls-known-servers"));

        ConfigBuilder configBuilder = ConfigBuilder.create()
                .serverPort(port)
                .serverHostname(url)
                .unixSocketFile(unixSocketFile)
                .sslAuthenticationMode(SslAuthenticationMode.valueOf(tls))
                .sslServerTrustMode(SslTrustModeFactory.resolveByLegacyValue(tlsservertrust))
                .sslServerTlsKeyPath(tlsserverkey)
                .sslServerTlsCertificatePath(tlsservercert)
                .sslServerTrustCertificates(tlsserverchain)
                .sslKnownClientsFile(tlsknownclients)
                .sslClientTrustMode(SslTrustModeFactory.resolveByLegacyValue(tlsclienttrust))
                .sslClientTlsKeyPath(tlsclientkey)
                .sslClientTlsCertificatePath(tlsclientcert)
                .sslClientTrustCertificates(tlsclientchain)
                .sslKnownServersFile(tlsknownservers)
                .peers(othernodes)
                .alwaysSendTo(alwaysSendToKeyPaths)
                .useWhiteList(useWhiteList)
                .keyData(keyData);

        Optional.ofNullable(storage)
                .map(JdbcConfigFactory::fromLegacyStorageString).ifPresent(configBuilder::jdbcConfig);

        return configBuilder.build();
    }

    static List<KeyDataConfig> createPrivateKeyData(List<String> privateKeys, List<String> privateKeyPasswords) {

        //Populate null values assume that they arent private
        List<String> passwordList = new ArrayList<>(privateKeyPasswords);
        for (int i = privateKeyPasswords.size() - 1; i < privateKeys.size(); i++) {
            passwordList.add(null);
        }

        List<JsonObject> privateKeyJson = privateKeys
                .stream()
                .map(s -> Paths.get(s))
                .map(path -> IOCallback.execute(() -> Files.newInputStream(path)))
                .map(Json::createReader)
                .map(JsonReader::readObject)
                .collect(Collectors.toList());

        List<KeyDataConfig> privateKeyData = IntStream
                .range(0, privateKeyJson.size())
                //FIXME: Canyt set to null value.. need to use addNull("password")
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
