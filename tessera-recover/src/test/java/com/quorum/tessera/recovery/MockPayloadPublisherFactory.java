package com.quorum.tessera.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;

import static org.mockito.Mockito.mock;

public class MockPayloadPublisherFactory implements PayloadPublisherFactory, BatchPayloadPublisherFactory {
    @Override
    public PayloadPublisher create(Config config) {
        return mock(PayloadPublisher.class);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

    @Override
    public BatchPayloadPublisher create(PayloadPublisher publisher) {
        return mock(BatchPayloadPublisher.class);
    }
}
