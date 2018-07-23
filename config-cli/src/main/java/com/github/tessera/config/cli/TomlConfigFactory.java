package com.github.tessera.config.cli;

import com.github.tessera.config.Config;
import com.github.tessera.config.ConfigFactory;
import com.github.tessera.config.PrivateKeyData;
import com.github.tessera.config.SslAuthenticationMode;
import com.github.tessera.config.SslTrustMode;
import com.github.tessera.config.util.JaxbUtil;
import com.github.tessera.io.FilesDelegate;
import com.github.tessera.io.IOCallback;
import com.moandjiezana.toml.Toml;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (keyConfigData.length != 0) {
            throw new UnsupportedOperationException("keyConfigData arg is not implemented for TomlConfigFactory");
        }

        Toml toml = new Toml().read(configData);

        if (LOGGER.isDebugEnabled()) {
            toml.toMap().entrySet().stream().forEach(entry -> {
                LOGGER.debug("Found entry in toml file : {} {}", entry.getKey(), entry.getValue());
            });
        }

        String url = toml.getString("url");

        String socket = toml.getString("socket");

        String tls = toml.getString("tls", "strict").toUpperCase();

        //??
        String workdir = toml.getString("workdir", ".");

        final List<String> othernodes = toml.getList("othernodes", Collections.EMPTY_LIST);

        final List<String> publicKeyList = toml.getList("publickeys", Collections.EMPTY_LIST);

        final List<String> privateKeyList = toml.getList("privatekeys", Collections.EMPTY_LIST);

        //String privateKeyPasswordFile = toml.getString("passwords")
        final List<String> privateKeyPasswords;
        if (toml.contains("passwords")) {
            String privateKeyPasswordFile = toml.getString("passwords");

            Path privateKeyPasswordFilePath = Paths.get(privateKeyPasswordFile);
            privateKeyPasswords = filesDelegate
                    .lines(privateKeyPasswordFilePath)
                    .collect(Collectors.toList());

        } else {
            privateKeyPasswords = Collections.unmodifiableList(Collections.EMPTY_LIST);
        }

        List<String> alwayssendtoList = toml.getList("alwayssendto", Collections.EMPTY_LIST);

        String tlsserverkey = toml.getString("tlsserverkey", "tls-server-key.pem");

        List<String> tlsserverchain = toml.getList("tlsserverchain", Collections.EMPTY_LIST);

        String storage = toml.getString("storage", "dir:storage");

        //verbosity
        final String tlsservercert = toml.getString("tlsservercert", "tls-server-cert.pem");

        final String tlsservertrust = toml.getString("tlsservertrust", "tofu");

        final String tlsclienttrust = toml.getString("tlsclienttrust", "ca-or-tofu");

        final String tlsknownservers = toml.getString("tlsknownservers", "tls-known-servers");
        
        final String tlsknownclients = toml.getString("tlsknownclients","tls-known-clients");
        
        ConfigBuilder configBuilder = ConfigBuilder.create()
                .serverHostname(url)
                .unixSocketFile(socket)
                .sslAuthenticationMode(SslAuthenticationMode.valueOf(tls))
                .sslServerKeyStorePath(tlsserverkey)
                .sslServerTrustMode(resolve(tlsservertrust))
                .sslServerTrustStorePath(tlsservertrust)
                .sslClientTrustMode(resolve(tlsclienttrust))
                .sslClientKeyStorePath(tlsserverkey)
                .sslClientKeyStorePassword("FIXME")
                .sslClientTrustStorePath(tlsservercert)
                .knownClientsFile(tlsknownclients)
                .knownServersFile(tlsknownservers)
                .peers(othernodes);

        return configBuilder.build();
    }

    static List<PrivateKeyData> createPrivateKeyData(List<String> privateKeys, List<String> privateKeyPasswords) {

        //Populate null values assume that they arent private
        List<String> passwordList = new ArrayList<>(privateKeyPasswords);
        for(int i = privateKeyPasswords.size() - 1; i < privateKeys.size();i++) {
            passwordList.add(null);
        }

        List<JsonObject> priavteKeyJson = privateKeys.stream()
                .map(s -> Paths.get(s))
                .map(path -> IOCallback.execute(() -> Files.newInputStream(path)))
                .map(is -> Json.createReader(is))
                .map(JsonReader::readObject)
                        .collect(Collectors.toList());
        
        
        List<PrivateKeyData> privateKeyData = IntStream.range(0, priavteKeyJson.size())
                //FIXME: Canyt set to null value.. need to use addNull("password")
                .mapToObj(i -> Json.createObjectBuilder(priavteKeyJson.get(i)).add("password",privateKeyPasswords.get(i)).build())
                .map(JsonObject::toString)
                .map(String::getBytes)
                .map(ByteArrayInputStream::new)
                .map(inputStream -> JaxbUtil.unmarshal(inputStream, PrivateKeyData.class))
                .collect(Collectors.toList());

        return Collections.unmodifiableList(privateKeyData);
    }

    //FIXME: CA-OR-TOFU 
    static SslTrustMode resolve(String value) {
        return Stream.of(SslTrustMode.values())
                .filter(s -> Objects.equals(s.name(), Objects.toString(value).toUpperCase()))
                .findFirst()
                .orElse(
                        Stream.of(SslTrustMode.CA, SslTrustMode.TOFU)
                                .filter(o -> Objects.equals("ca-or-tofu", value))
                                .findAny()
                                .orElse(SslTrustMode.NONE));
    }

}
