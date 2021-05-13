package com.quorum.tessera.privacygroup.publish;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.Config;

public class MockPrivacyGroupPublisherFactory implements PrivacyGroupPublisherFactory {
  @Override
  public PrivacyGroupPublisher create(Config config) {
    return mock(PrivacyGroupPublisher.class);
  }
}
