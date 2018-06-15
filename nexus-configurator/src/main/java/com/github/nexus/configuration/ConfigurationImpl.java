package com.github.nexus.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
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
    public String privateKeys() {
        return properties.getProperty("privateKeys");
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
