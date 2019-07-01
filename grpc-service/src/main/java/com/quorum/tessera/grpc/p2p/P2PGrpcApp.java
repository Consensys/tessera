package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.grpc.GrpcApp;
import com.quorum.tessera.service.locator.ServiceLocator;

public class P2PGrpcApp extends GrpcApp implements TesseraApp {

  public P2PGrpcApp() {
    this(ServiceLocator.create(), "tessera-core-spring.xml");
  }

  public P2PGrpcApp(ServiceLocator serviceLocator, String contextName) {
    super(serviceLocator, contextName);
  }

  @Override
  public AppType getAppType() {
    return AppType.P2P;
  }
}
