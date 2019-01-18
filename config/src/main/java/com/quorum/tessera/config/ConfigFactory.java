package com.quorum.tessera.config;

import com.quorum.tessera.config.keypairs.ConfigKeyPair;

import java.io.InputStream;
import java.util.List;
import java.util.ServiceLoader;

public interface ConfigFactory {

    Config create(InputStream configData, List<ConfigKeyPair> newkeys);

    static ConfigFactory create() {
        return ServiceLoader.load(ConfigFactory.class).iterator().next();
    }

}
