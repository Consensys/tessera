package com.quorum.tessera.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RuntimeContextFactoryTest extends ContextTestCase {

  @Test
  public void newFactory() {

    RuntimeContextFactory runtimeContextFactory = RuntimeContextFactory.newFactory();

    assertThat(runtimeContextFactory).isExactlyInstanceOf(MockRuntimeContextFactory.class);
  }
}
