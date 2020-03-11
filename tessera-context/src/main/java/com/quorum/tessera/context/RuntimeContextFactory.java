package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;

import java.lang.reflect.Proxy;

public interface RuntimeContextFactory<T> {

    RuntimeContext create(T config);

    static RuntimeContextFactory newFactory() {

        RuntimeContextFactory factory =
                ServiceLoaderUtil.load(RuntimeContextFactory.class).orElse(new DefaultRuntimeContextFactory());
        return (RuntimeContextFactory)
                Proxy.newProxyInstance(
                        RuntimeContextFactory.class.getClassLoader(),
                        new Class[] {RuntimeContextFactory.class},
                        new CreateContextInvocationHandler(factory));
    }
}
