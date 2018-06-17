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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
    public String workdir() {
        return properties.getProperty("workdir");
    }

    @Override
    public String socket() {
        return properties.getProperty("socket");
    }
}
