package com.quorum.tessera.config;

import com.quorum.tessera.loader.ServiceLoaderUtil;

import java.io.InputStream;

public interface ConfigFactory {

    Config create(InputStream configData);

    static ConfigFactory create() {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(ConfigFactory.class).findAny().get();
    }
}
