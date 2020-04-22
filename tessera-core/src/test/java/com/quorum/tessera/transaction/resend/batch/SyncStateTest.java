package com.quorum.tessera.transaction.resend.batch;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SyncStateTest {

    @Test
    public void create() {
        SyncState result = SyncState.create();
        assertThat(result).isExactlyInstanceOf(MockSyncState.class);
    }

    @Test
    public void toggleResendMode() {
        SyncState syncState = SyncStateImpl.INSTANCE;
        assertThat(syncState.isResendMode()).isFalse();
        assertThat(syncState.setResendMode(true)).isFalse();
        assertThat(syncState.isResendMode()).isTrue();
    }
}
