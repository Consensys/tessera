package com.quorum.tessera.context;

import java.util.ServiceLoader;

public interface RuntimeContextFactory<T> {

    RuntimeContext create(T config);

    static RuntimeContextFactory newFactory() {
        return ServiceLoader.load(RuntimeContextFactory.class).findFirst()
                    .orElseGet(DefaultRuntimeContextFactory::new);
    }

}
