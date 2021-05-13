package com.quorum.tessera.p2p.resend;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

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
