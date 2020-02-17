package com.quorum.tessera.context;

import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RuntimeContextFactoryTest extends ContextTestCase {

    @Test
    public void newFactory() {

        RuntimeContextFactory runtimeContextFactory = RuntimeContextFactory.newFactory();

        assertThat(Proxy.isProxyClass(runtimeContextFactory.getClass())).isTrue();

        assertThat(ContextHolder.INSTANCE.getContextFactory().get()).isSameAs(runtimeContextFactory);
    }

    @Test
    public void newFactoryWhenExists() {
        RuntimeContextFactory runtimeContextFactory = mock(RuntimeContextFactory.class);
        ContextHolder.INSTANCE.setContextFactory(runtimeContextFactory);
        RuntimeContextFactory result = RuntimeContextFactory.newFactory();
        assertThat(runtimeContextFactory).isSameAs(result);
    }
}
