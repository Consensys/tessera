package com.quorum.tessera.context;

import java.util.Optional;

/*
Quick and dirty way of storing a single
RuntimeContextFactory and RuntimeContext instance
 */
enum ContextHolder {
    INSTANCE;

    private RuntimeContextFactory runtimeContextFactory;

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

    protected Optional<RuntimeContextFactory> getContextFactory() {
        return Optional.ofNullable(runtimeContextFactory);
    }

    protected ContextHolder setContextFactory(RuntimeContextFactory runtimeContextFactory) {
        if (this.runtimeContextFactory != null) {
            throw new IllegalStateException("RuntimeContextFactory has already been stored");
        }
        this.runtimeContextFactory = runtimeContextFactory;
        return this;
    }
}
