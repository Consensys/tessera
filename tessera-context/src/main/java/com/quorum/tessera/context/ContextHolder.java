package com.quorum.tessera.context;

import java.util.Optional;

/*
RuntimeContextFactory and RuntimeContext instance
 */
enum ContextHolder {
    INSTANCE;

    private RuntimeContext runtimeContext;

    protected Optional<RuntimeContext> getContext() {
        return Optional.ofNullable(runtimeContext);
    }

    protected ContextHolder setContext(RuntimeContext runtimeContext) {
        if (this.runtimeContext != null) {
            throw new IllegalStateException("RuntimeContext has already been stored");
        }
        this.runtimeContext = runtimeContext;
        return this;
    }

}
