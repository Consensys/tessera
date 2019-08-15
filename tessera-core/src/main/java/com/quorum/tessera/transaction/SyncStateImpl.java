package com.quorum.tessera.transaction;

import java.util.concurrent.atomic.AtomicBoolean;

public enum SyncStateImpl implements SyncState {

    INSTANCE;

    private final AtomicBoolean resendMode = new AtomicBoolean(false);

    @Override
    public boolean setResendMode(boolean value) {
        return resendMode.getAndSet(value);
    }

    @Override
    public boolean isResendMode() {
        return resendMode.get();
    }

}
