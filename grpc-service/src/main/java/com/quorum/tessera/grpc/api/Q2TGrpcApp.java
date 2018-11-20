package com.quorum.tessera.grpc.api;

import com.quorum.tessera.config.apps.Q2TApp;
import com.quorum.tessera.grpc.GrpcApp;
import com.quorum.tessera.service.locator.ServiceLocator;

public class Q2TGrpcApp extends GrpcApp implements Q2TApp {
    public Q2TGrpcApp(ServiceLocator serviceLocator, String contextName) {
        super(serviceLocator, contextName);
    }
}
