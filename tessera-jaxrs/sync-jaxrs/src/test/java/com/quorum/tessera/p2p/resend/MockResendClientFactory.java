package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.p2p.resend.ResendClient;
import com.quorum.tessera.p2p.resend.ResendClientFactory;

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
