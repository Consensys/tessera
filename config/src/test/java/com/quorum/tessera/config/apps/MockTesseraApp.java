package com.quorum.tessera.config.apps;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;

public class MockTesseraApp implements TesseraApp {

  @Override
  public CommunicationType getCommunicationType() {
    return CommunicationType.REST;
  }

  @Override
  public AppType getAppType() {
    return AppType.P2P;
  }
}
