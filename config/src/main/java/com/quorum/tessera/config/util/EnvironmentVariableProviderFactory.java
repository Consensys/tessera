package com.quorum.tessera.config.util;

import java.util.ServiceLoader;

/**
 * Exists to enable the loading of a mocked EnvironmentVariableProvider in tests
 */
public interface EnvironmentVariableProviderFactory {

    EnvironmentVariableProvider create();

    static EnvironmentVariableProviderFactory load() {
        return ServiceLoader.load(EnvironmentVariableProviderFactory.class).iterator().next();
    }

}
