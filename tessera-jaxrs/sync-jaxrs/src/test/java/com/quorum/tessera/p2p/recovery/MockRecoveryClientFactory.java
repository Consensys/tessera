package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import static org.mockito.Mockito.mock;

public class MockRecoveryClientFactory implements RecoveryClientFactory {
    @Override
    public RecoveryClient create(Config config) {
        return mock(RecoveryClient.class);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
