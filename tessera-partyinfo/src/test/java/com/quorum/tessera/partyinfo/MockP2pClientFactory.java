package com.quorum.tessera.partyinfo;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

public class MockP2pClientFactory implements P2pClientFactory {

  @Override
  public P2pClient create(Config config) {
    return mock(P2pClient.class);
  }

  @Override
  public CommunicationType communicationType() {
    return CommunicationType.REST;
  }
}
