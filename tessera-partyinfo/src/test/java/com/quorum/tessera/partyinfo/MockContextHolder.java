package com.quorum.tessera.partyinfo;

import com.quorum.tessera.context.ContextHolder;
import com.quorum.tessera.context.RuntimeContext;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class MockContextHolder implements ContextHolder {

    static ThreadLocal<RuntimeContext> runtimeContextThreadLocal = ThreadLocal.withInitial(() -> mock(RuntimeContext.class));

    @Override
    public void setContext(RuntimeContext runtimeContext) {
        runtimeContextThreadLocal.set(runtimeContext);
    }

    static void reset() {
        runtimeContextThreadLocal.remove();
    }

    @Override
    public Optional<RuntimeContext> getContext() {
        return Optional.of(runtimeContextThreadLocal.get());
    }
}
