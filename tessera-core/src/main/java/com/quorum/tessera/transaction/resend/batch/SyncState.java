package com.quorum.tessera.transaction.resend.batch;

import com.quorum.tessera.ServiceLoaderUtil;

public interface SyncState {

    boolean setResendMode(boolean value);

    boolean isResendMode();

    static SyncState create() {
        return ServiceLoaderUtil.load(SyncState.class).orElse(SyncStateImpl.INSTANCE);
    }
}
