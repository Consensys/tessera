package com.quorum.tessera.q2t;

import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.service.locator.ServiceLocator;
import io.swagger.annotations.Api;

import java.util.Set;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@Api
@ApplicationPath("/")
public class Q2TRestApp extends TesseraRestApplication {

    private final ServiceLocator serviceLocator;

    public Q2TRestApp() {
        this(ServiceLocator.create());
    }

    public Q2TRestApp(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public Set<Object> getSingletons() {

        TransactionResource transactionResource = new TransactionResource();

        return Set.of(transactionResource);
    }

    @Override
    public AppType getAppType() {
        return AppType.Q2T;
    }
}
