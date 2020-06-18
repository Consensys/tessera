package com.quorum.tessera.context;

import com.quorum.tessera.config.Config;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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

        assertThat(DefaultContextHolder.INSTANCE.getContext().get()).isSameAs(runtimeContext);

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

    @Test
    public void invocationTargetExceptionCaughtAndCauseThrown() throws Exception {
        class TestObj implements RuntimeContextFactory {
            @Override
            public RuntimeContext create(Object config) {
                throw new IllegalStateException();
            }
        }

        RuntimeContextFactory factory = new TestObj();
        CreateContextInvocationHandler handler = new CreateContextInvocationHandler(factory);

        Object proxy = mock(TestObj.class);
        Method method = TestObj.class.getMethod("create", Object.class);
        Object[] args = {mock(Object.class)};

        Throwable ex = catchThrowable(() -> handler.invoke(proxy, method, args));

        assertThat(ex).isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    public void createMethodReturnsExsitingInstance() {

        Config config = mock(Config.class);

        RuntimeContextFactory factory = mock(RuntimeContextFactory.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(factory.create(config)).thenReturn(runtimeContext);
        DefaultContextHolder.INSTANCE.setContext(runtimeContext);

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

        assertThat(DefaultContextHolder.INSTANCE.getContext().get()).isSameAs(runtimeContext);

        verifyNoMoreInteractions(factory);
    }
}
