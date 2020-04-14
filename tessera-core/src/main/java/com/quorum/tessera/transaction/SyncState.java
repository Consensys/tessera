package com.quorum.tessera.transaction;

import com.quorum.tessera.ServiceLoaderUtil;

public interface SyncState {

    boolean setResendMode(boolean value);

    boolean isResendMode();

    static SyncState create() {
        return ServiceLoaderUtil.load(SyncState.class).orElse(SyncStateImpl.INSTANCE);
    }
}
