package com.quorum.tessera.config.util;

import com.quorum.tessera.loader.ServiceLoaderUtil;

/** Exists to enable the loading of a mocked EnvironmentVariableProvider in tests */
public interface EnvironmentVariableProviderFactory {

    EnvironmentVariableProvider create();

    static EnvironmentVariableProviderFactory load() {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(EnvironmentVariableProviderFactory.class).findAny().get();
    }
}
