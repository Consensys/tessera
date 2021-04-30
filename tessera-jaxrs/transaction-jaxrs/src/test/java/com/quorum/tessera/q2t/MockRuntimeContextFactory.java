package com.quorum.tessera.q2t;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.ContextHolder;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class MockRuntimeContextFactory implements RuntimeContextFactory<Config>, ContextHolder {
    static ThreadLocal<RuntimeContext> runtimeContextThreadLocal =
            ThreadLocal.withInitial(() -> mock(RuntimeContext.class));

    @Override
    public void setContext(RuntimeContext runtimeContext) {
        runtimeContextThreadLocal.set(runtimeContext);
    }

    public static void reset() {
        runtimeContextThreadLocal.remove();
    }

    @Override
    public RuntimeContext create(Config config) {
        return runtimeContextThreadLocal.get();
    }

    @Override
    public Optional<RuntimeContext> getContext() {
        return Optional.of(runtimeContextThreadLocal.get());
    }
}
