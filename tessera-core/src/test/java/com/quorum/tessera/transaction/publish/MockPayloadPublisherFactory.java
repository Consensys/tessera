package com.quorum.tessera.transaction.publish;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

public class MockPayloadPublisherFactory implements PayloadPublisherFactory {

  private static CommunicationType communicationType = CommunicationType.REST;

  static void setCommunicationType(CommunicationType c) {
    communicationType = c;
  }

  @Override
  public PayloadPublisher create(Config config) {
    return mock(PayloadPublisher.class);
  }

  @Override
  public CommunicationType communicationType() {
    return communicationType;
  }
}
