package com.quorum.tessera.context;

import com.quorum.tessera.loader.ServiceLoaderUtil;

public interface RuntimeContextFactory<T> {

    RuntimeContext create(T config);

    static RuntimeContextFactory newFactory() {
        return ServiceLoaderUtil.load(RuntimeContextFactory.class)
                    .orElseGet(DefaultRuntimeContextFactory::new);
    }

}
