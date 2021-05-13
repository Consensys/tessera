package com.quorum.tessera.recovery;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.resend.ResendBatchPublisherFactory;

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
