package com.quorum.tessera.context;

import com.quorum.tessera.config.Config;
import org.junit.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CreateContextInvocationHandlerTest extends ContextTestCase {

    @Test
    public void createProxyAndCreate() {

        Config config = mock(Config.class);

        RuntimeContextFactory factory = mock(RuntimeContextFactory.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(factory.create(config)).thenReturn(runtimeContext);

        CreateContextInvocationHandler handler = new CreateContextInvocationHandler(factory);

        RuntimeContextFactory proxy =
                (RuntimeContextFactory)
                        Proxy.newProxyInstance(
                                RuntimeContextFactory.class.getClassLoader(),
                                new Class[] {RuntimeContextFactory.class},
                                handler);

        RuntimeContext result = proxy.create(config);
        assertThat(result).isSameAs(runtimeContext);

        verify(factory).create(config);

        assertThat(ContextHolder.INSTANCE.getContext().get()).isSameAs(runtimeContext);

        verifyNoMoreInteractions(factory);
    }

    @Test
    public void createProxyAndCallNonCreateMethod() {

        Config config = mock(Config.class);

        RuntimeContextFactory factory = mock(RuntimeContextFactory.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(factory.create(config)).thenReturn(runtimeContext);

        CreateContextInvocationHandler handler = new CreateContextInvocationHandler(factory);

        RuntimeContextFactory proxy =
                (RuntimeContextFactory)
                        Proxy.newProxyInstance(
                                RuntimeContextFactory.class.getClassLoader(),
                                new Class[] {RuntimeContextFactory.class},
                                handler);

        proxy.equals(factory);

        verifyNoMoreInteractions(factory);
    }
}
