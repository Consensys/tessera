package com.quorum.tessera.recover.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import com.quorum.tessera.partyinfo.ResendBatchPublisherFactory;

import static org.mockito.Mockito.mock;

public class MockResendBatchPublisherFactory implements ResendBatchPublisherFactory {
    @Override
    public ResendBatchPublisher create(Config config) {
        return mock(ResendBatchPublisher.class);
    }

    @Override
    public CommunicationType communicationType() {
        return getCommunicationType();
    }


    static CommunicationType getCommunicationType() {
        return CommunicationType.REST;
    }
}
