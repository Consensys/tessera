package com.quorum.tessera.context;

import java.util.Objects;
import java.util.Optional;

/*
RuntimeContextFactory and RuntimeContext instance
 */
enum DefaultContextHolder implements ContextHolder {
    INSTANCE;

    private RuntimeContext runtimeContext;

    public Optional<RuntimeContext> getContext() {
        return Optional.ofNullable(runtimeContext);
    }

    public void setContext(RuntimeContext runtimeContext) {

        if (this.runtimeContext != null) {
            throw new IllegalStateException("RuntimeContext has already been stored");
        }
        this.runtimeContext = Objects.requireNonNull(runtimeContext, "Runtime context cannot be null");
    }
}
