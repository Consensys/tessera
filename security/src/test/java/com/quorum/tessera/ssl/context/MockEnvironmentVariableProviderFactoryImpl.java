package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.EnvironmentVariableProviderFactory;

import static org.mockito.Mockito.mock;

public class MockEnvironmentVariableProviderFactoryImpl implements EnvironmentVariableProviderFactory {

    private static final EnvironmentVariableProvider envVarProvider = mock(EnvironmentVariableProvider.class);

    @Override
    public EnvironmentVariableProvider create() {
        return envVarProvider;
    }
}
