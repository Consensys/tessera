package com.quorum.tessera.app;

import com.quorum.tessera.config.AppType;

public class SampleApp extends TesseraRestApplication {

  @Override
  public AppType getAppType() {
    return AppType.Q2T;
  }
}
