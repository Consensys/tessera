package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

import java.io.InputStream;
import java.util.List;

public class MockConfigFactory implements ConfigFactory {

    @Override
    public Config create(InputStream configData, List<ConfigKeyPair> newkeys) {
        Config config = new Config();

        return config;
    }
}
