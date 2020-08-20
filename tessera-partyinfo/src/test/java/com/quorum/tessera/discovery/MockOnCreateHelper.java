package com.quorum.tessera.discovery;

import java.util.concurrent.atomic.AtomicInteger;

public class MockOnCreateHelper implements OnCreateHelper {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Override
    public void onCreate() {
        COUNTER.incrementAndGet();
    }

    static void reset() {
        COUNTER.set(0);
    }

    int getInvocationCount() {
        return COUNTER.get();
    }
}
