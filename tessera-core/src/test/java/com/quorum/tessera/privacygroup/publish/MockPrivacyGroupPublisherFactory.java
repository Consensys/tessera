package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.config.Config;

import static org.mockito.Mockito.mock;

public class MockPrivacyGroupPublisherFactory implements PrivacyGroupPublisherFactory {
    @Override
    public PrivacyGroupPublisher create(Config config) {
        return mock(PrivacyGroupPublisher.class);
    }
}
