package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.*;

import java.lang.reflect.Proxy;

public interface RuntimeContextFactory {

    RuntimeContext create(Config config);

    static RuntimeContextFactory newFactory() {

        RuntimeContextFactory factory =
                ServiceLoaderUtil.load(RuntimeContextFactory.class)
                    .orElse(new DefaultRuntimeContextFactory());
        return
                (RuntimeContextFactory)
                        Proxy.newProxyInstance(
                                RuntimeContextFactory.class.getClassLoader(),
                                new Class[] {RuntimeContextFactory.class},
                                new CreateContextInvocationHandler(factory));
    }
}
