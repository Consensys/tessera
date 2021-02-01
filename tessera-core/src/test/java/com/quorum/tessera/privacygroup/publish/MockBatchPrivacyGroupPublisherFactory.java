package com.quorum.tessera.privacygroup.publish;

import static org.mockito.Mockito.mock;

public class MockBatchPrivacyGroupPublisherFactory implements BatchPrivacyGroupPublisherFactory {

    @Override
    public BatchPrivacyGroupPublisher create(PrivacyGroupPublisher privacyGroupPublisher) {
        return mock(BatchPrivacyGroupPublisher.class);
    }
}
