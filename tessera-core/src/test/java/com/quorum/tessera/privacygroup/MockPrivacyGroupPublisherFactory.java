package com.quorum.tessera.privacygroup;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisherFactory;

import static org.mockito.Mockito.mock;

public class MockPrivacyGroupPublisherFactory implements PrivacyGroupPublisherFactory {

    @Override
    public PrivacyGroupPublisher create(Config config) {
        return mock(PrivacyGroupPublisher.class);
    }
}
