package com.quorum.tessera.context;

import com.quorum.tessera.ServiceLoaderUtil;

import java.util.Optional;

public interface ContextHolder {

    Optional<RuntimeContext> getContext();

    static ContextHolder getInstance() {
        return ServiceLoaderUtil.load(ContextHolder.class)
            .orElse(DefaultContextHolder.INSTANCE);
    }

}
