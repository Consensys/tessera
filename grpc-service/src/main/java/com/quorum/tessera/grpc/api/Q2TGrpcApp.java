package com.quorum.tessera.grpc.api;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.grpc.GrpcApp;
import com.quorum.tessera.service.locator.ServiceLocator;

public class Q2TGrpcApp extends GrpcApp implements TesseraApp {

  public Q2TGrpcApp() {
    this(ServiceLocator.create(), "tessera-core-spring.xml");
  }

  public Q2TGrpcApp(ServiceLocator serviceLocator, String contextName) {
    super(serviceLocator, contextName);
  }

  @Override
  public AppType getAppType() {
    return AppType.Q2T;
  }
}
