package com.quorum.tessera.config;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

import java.io.InputStream;
import java.util.List;

public interface ConfigFactory {

    Config create(InputStream configData, List<ConfigKeyPair> newkeys);

    static ConfigFactory create() {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(ConfigFactory.class).findAny().get();
    }
}
