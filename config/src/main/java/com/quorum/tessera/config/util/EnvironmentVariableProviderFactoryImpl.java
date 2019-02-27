package com.quorum.tessera.config.util;

import java.util.ServiceLoader;

public class EnvironmentVariableProviderFactoryImpl implements EnvironmentVariableProviderFactory {

    @Override
    public EnvironmentVariableProvider create() {
        return new EnvironmentVariableProvider();
    }

    static EnvironmentVariableProviderFactoryImpl load() {
        return ServiceLoader.load(EnvironmentVariableProviderFactoryImpl.class).iterator().next();
    }

}
