package com.quorum.tessera.context;

import com.quorum.tessera.config.Config;

import static org.mockito.Mockito.mock;

public class MockRuntimeContextFactory implements RuntimeContextFactory {
    @Override
    public RuntimeContext create(Config config) {
        return mock(RuntimeContext.class);
    }
}
