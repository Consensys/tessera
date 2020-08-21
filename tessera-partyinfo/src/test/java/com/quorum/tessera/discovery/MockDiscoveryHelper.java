package com.quorum.tessera.discovery;

import com.quorum.tessera.partyinfo.node.NodeInfo;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

public class MockDiscoveryHelper implements DiscoveryHelper {

    private static final AtomicInteger ONCREATE_COUNTER = new AtomicInteger(0);

    private static final AtomicInteger BUILDCURRENT_COUNTER = new AtomicInteger(0);

    @Override
    public NodeInfo buildCurrent() {
        BUILDCURRENT_COUNTER.incrementAndGet();
        return mock(NodeInfo.class);
    }

    @Override
    public void onCreate() {
        ONCREATE_COUNTER.incrementAndGet();
    }

    static void reset() {
        ONCREATE_COUNTER.set(0);
        BUILDCURRENT_COUNTER.set(0);
    }

    int getOnCreateInvocationCount() {
        return ONCREATE_COUNTER.get();
    }

    int getBuildCurrentInvocationCounter() {
        return BUILDCURRENT_COUNTER.get();
    }
}
