
package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


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
