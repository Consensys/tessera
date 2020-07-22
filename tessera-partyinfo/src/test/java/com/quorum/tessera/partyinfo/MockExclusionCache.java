package com.quorum.tessera.partyinfo;

public class MockExclusionCache implements ExclusionCache<Object> {
    @Override
    public boolean isExcluded(Object recipient) {
        return false;
    }

    @Override
    public ExclusionCache<Object> exclude(Object recipient) {
        return this;
    }

    @Override
    public ExclusionCache<Object> start() {
        return this;
    }

    @Override
    public void stop() {

    }
}
