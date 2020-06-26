package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;

public interface RuntimeContextFactory<T> {

    RuntimeContext create(T config);

    static RuntimeContextFactory newFactory() {
        return ServiceLoaderUtil.load(RuntimeContextFactory.class)
                    .orElseGet(DefaultRuntimeContextFactory::new);
    }

}
