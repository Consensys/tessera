package com.github.nexus.configuration;

import com.github.nexus.configuration.model.KeyData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.UriBuilder;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ConfigurationImpl implements Configuration {

    private final Properties properties;

    public ConfigurationImpl(final Properties properties) {
        this.properties = (Properties) properties.clone();
    }

    @Override
    public Path keygenBasePath() {
        return Paths.get(properties.getProperty("keygenBasePath")).toAbsolutePath();
    }

    @Override
    public List<String> publicKeys() {
        return Arrays.asList(properties.getProperty("publicKeys").split(","));
    }

    @Override
    public List<JsonObject> privateKeys() {
        return Json
            .createReader(new StringReader("[" + properties.getProperty("privateKeys") + "]"))
            .readArray()
            .getValuesAs(JsonValue::asJsonObject);
    }

    @Override
    public List<String> passwords() {
        return Arrays.asList(properties.getProperty("passwords").split(",", -1));
    }

    @Override
    public List<KeyData> keyData() {
        final List<String> publicKeys = this.publicKeys();
        final List<JsonObject> privateKeys = this.privateKeys();
        final List<String> passwords = this.passwords();

        if((publicKeys.size() != privateKeys.size()) || (publicKeys.size() != passwords.size())) {
            throw new RuntimeException("Public, private keys and passwords must match up");
        }

        return IntStream
            .range(0, passwords.size())
            .mapToObj(i -> new KeyData(publicKeys.get(i), privateKeys.get(i), passwords.get(i)))
            .collect(toList());
    }

    @Override
    public String url() {
        return properties.getProperty("url");
    }

    @Override
    public int port() {
        return Integer.valueOf(properties.getProperty("port"));
    }

    @Override
    public URI uri() {
        return UriBuilder.fromUri(url()).port(port()).build();
    }

    @Override
    public List<String> othernodes() {
        return Arrays.asList(properties.getProperty("othernodes").split(","));
    }

    @Override
    public List<String> generatekeys() {
        return Stream
            .of(properties.getProperty("generatekeys").split(","))
            .filter(str -> !str.isEmpty())
            .collect(toList());
    }

    @Override
    public Set<String> whitelist() {
        return Stream.of(properties.getProperty("whitelist").split(","))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(toSet());
    }

    @Override
    public String workdir() {
        return properties.getProperty("workdir");
    }

    @Override
    public String socket() {
        return properties.getProperty("socket");
    }

    @Override
    public String databaseURL() {
        return properties.getProperty("databaseURL");
    }

    @Override
    public String tls() {
        return properties.getProperty("tls");
    }

    @Override
    public String serverKeyStore() {
        return properties.getProperty("serverKeyStore");
    }

    @Override
    public String serverKeyStorePassword() {
        return properties.getProperty("serverKeyStorePassword");
    }

    @Override
    public String serverTrustStore() {
        return properties.getProperty("serverTrustStore");
    }

    @Override
    public String serverTrustStorePassword() {
        return properties.getProperty("serverTrustStorePassword");
    }

    @Override
    public String serverTrustMode() {
        return properties.getProperty("serverTrustMode");
    }

    @Override
    public String knownClients() {
        return properties.getProperty("knownClients");
    }

    @Override
    public String clientKeyStore() {
        return properties.getProperty("clientKeyStore");
    }

    @Override
    public String clientKeyStorePassword() {
        return properties.getProperty("clientKeyStorePassword");
    }

    @Override
    public String clientTrustStore() {
        return properties.getProperty("clientTrustStore");
    }

    @Override
    public String clientTrustStorePassword() {
        return properties.getProperty("clientTrustStorePassword");
    }

    @Override
    public String clientTrustMode() {
        return properties.getProperty("clientTrustMode");
    }

    @Override
    public String knownServers() {
        return properties.getProperty("knownServers");
    }
}
