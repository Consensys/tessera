package com.github.nexus.configuration;

import com.github.nexus.configuration.model.KeyData;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        return Stream.of(properties.getProperty("publicKeys").split(",")).collect(Collectors.toList());
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
        return Stream.of(properties.getProperty("passwords").split(",", -1)).collect(Collectors.toList());
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
            .collect(Collectors.toList());
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
    public List<String> othernodes() {
        return Stream.of(properties.getProperty("othernodes").split(",")).collect(Collectors.toList());
    }
}
