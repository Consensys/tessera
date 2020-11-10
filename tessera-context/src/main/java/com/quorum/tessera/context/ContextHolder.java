package com.quorum.tessera.context;

import java.util.Optional;
import java.util.ServiceLoader;

public interface ContextHolder {

    Optional<RuntimeContext> getContext();

    void setContext(RuntimeContext runtimeContext);

    static ContextHolder getInstance() {
        return ServiceLoader.load(ContextHolder.class).findFirst()
            .orElse(DefaultContextHolder.INSTANCE);
    }

}
