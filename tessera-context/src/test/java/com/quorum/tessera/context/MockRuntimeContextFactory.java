package com.quorum.tessera.context;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.Config;

public class MockRuntimeContextFactory implements RuntimeContextFactory<Config> {
  @Override
  public RuntimeContext create(Config config) {
    return mock(RuntimeContext.class);
  }
}
