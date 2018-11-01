package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.apps.P2PApp;
import com.quorum.tessera.grpc.GrpcApp;
import com.quorum.tessera.service.locator.ServiceLocator;

public class P2PGrpcApp extends GrpcApp implements P2PApp {
    public P2PGrpcApp(ServiceLocator serviceLocator, String contextName) {
        super(serviceLocator, contextName);
    }
}
