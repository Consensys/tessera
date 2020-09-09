package com.quorum.tessera.p2p;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.ContextHolder;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.context.RuntimeContextFactory;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockRuntimeContextFactory implements RuntimeContextFactory<Config>, ContextHolder {
    static ThreadLocal<RuntimeContext> runtimeContextThreadLocal =
        ThreadLocal.withInitial(
            () -> {
                RuntimeContext mockContext = mock(RuntimeContext.class);
                when(mockContext.getP2pServerUri()).thenReturn(URI.create("http://own.com/"));
                when(mockContext.getPeers()).thenReturn(List.of(URI.create("http://peer.com/")));
                return mockContext;
            });

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
