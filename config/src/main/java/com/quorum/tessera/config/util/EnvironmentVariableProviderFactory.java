package com.quorum.tessera.config.util;

import java.util.ServiceLoader;

public interface EnvironmentVariableProviderFactory {

    EnvironmentVariableProvider create();

    static EnvironmentVariableProviderFactory load() {
        return ServiceLoader.load(EnvironmentVariableProviderFactory.class).iterator().next();
    }

}
