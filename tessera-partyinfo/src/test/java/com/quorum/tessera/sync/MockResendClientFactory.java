package com.quorum.tessera.sync;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import static org.mockito.Mockito.mock;

public class MockResendClientFactory implements ResendClientFactory {
    @Override
    public ResendClient create(Config config) {
        return mock(ResendClient.class);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
