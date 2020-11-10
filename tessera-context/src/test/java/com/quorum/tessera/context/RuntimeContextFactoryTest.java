package com.quorum.tessera.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeContextFactoryTest extends ContextTestCase {

    @Test
    public void newFactory() {

        RuntimeContextFactory runtimeContextFactory = RuntimeContextFactory.newFactory();

        assertThat(runtimeContextFactory).isExactlyInstanceOf(DefaultRuntimeContextFactory.class);
    }
}
