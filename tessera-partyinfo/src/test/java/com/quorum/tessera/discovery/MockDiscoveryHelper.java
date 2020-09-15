package com.quorum.tessera.discovery;

import com.openpojo.validation.test.impl.SerializableTester;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.node.NodeInfo;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

public class MockDiscoveryHelper implements DiscoveryHelper {

    private static final AtomicInteger ONCREATE_COUNTER = new AtomicInteger(0);

    private static final AtomicInteger BUILDCURRENT_COUNTER = new AtomicInteger(0);

    private static final AtomicInteger BUILDREMOTE_COUNTER = new AtomicInteger(0);

    private static final AtomicInteger BUILDALL_COUNTER = new AtomicInteger(0);

    @Override
    public NodeInfo buildCurrent() {
        BUILDCURRENT_COUNTER.incrementAndGet();
        return mock(NodeInfo.class);
    }

    @Override
    public void onCreate() {
        ONCREATE_COUNTER.incrementAndGet();
    }

    @Override
    public NodeInfo buildRemoteNodeInfo(PublicKey publicKey) {
        BUILDREMOTE_COUNTER.incrementAndGet();
        return mock(NodeInfo.class);
    }

    @Override
    public Set<NodeInfo> buildRemoteNodeInfos() {
        BUILDALL_COUNTER.incrementAndGet();
        return mock(Set.class);
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

    int getBuildRemoteInvocationCounter() {
        return BUILDREMOTE_COUNTER.get();
    }

    int getBuildAllInvocationCounter() {
        return BUILDALL_COUNTER.get();
    }
}
