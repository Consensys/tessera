package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import static org.mockito.Mockito.mock;

public class MockResendBatchPublisherFactory implements ResendBatchPublisherFactory {

    @Override
    public ResendBatchPublisher create(Config config) {
        return mock(ResendBatchPublisher.class);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
