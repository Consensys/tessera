package com.quorum.tessera.transaction.resend.batch;

import com.quorum.tessera.transaction.resend.batch.SyncState;

import java.util.concurrent.atomic.AtomicBoolean;

public class MockSyncState implements SyncState {

    private final AtomicBoolean resendMode = new AtomicBoolean(false);

    @Override
    public boolean setResendMode(boolean resendMode) {
        return this.resendMode.getAndSet(resendMode);
    }

    @Override
    public boolean isResendMode() {
        return resendMode.get();
    }
}
