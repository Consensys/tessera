package com.quorum.tessera.context;

import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

public class RuntimeContextFactoryTest extends ContextTestCase {

    @Test
    public void newFactory() {

        RuntimeContextFactory runtimeContextFactory = RuntimeContextFactory.newFactory();

        assertThat(Proxy.isProxyClass(runtimeContextFactory.getClass())).isTrue();
    }

}
