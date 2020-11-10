package com.quorum.tessera.config;

import java.io.InputStream;
import java.util.ServiceLoader;

public interface ConfigFactory {

    Config create(InputStream configData);

    static ConfigFactory create() {
        return ServiceLoader.load(ConfigFactory.class).findFirst().get();
    }
}
