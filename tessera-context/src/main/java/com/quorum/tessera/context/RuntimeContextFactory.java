package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;
import java.lang.reflect.Proxy;

public interface RuntimeContextFactory<T> {

    RuntimeContext create(T config);

    static RuntimeContextFactory newFactory() {

        return ServiceLoaderUtil.load(RuntimeContextFactory.class)
                    .orElseGet(() -> (RuntimeContextFactory) Proxy.newProxyInstance(
                        RuntimeContextFactory.class.getClassLoader(),
                        new Class[] {RuntimeContextFactory.class},
                        new CreateContextInvocationHandler(new DefaultRuntimeContextFactory())));
    }
}
