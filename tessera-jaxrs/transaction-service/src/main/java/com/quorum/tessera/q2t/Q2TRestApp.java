package com.quorum.tessera.q2t;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.app.TesseraRestApplication;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server Contains all the service classes created by the service
 * locator
 */
@GlobalFilter
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

        IPWhitelistFilter iPWhitelistFilter = new IPWhitelistFilter();
        TransactionResource transactionResource = new TransactionResource();

        return Stream.of(iPWhitelistFilter, transactionResource).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.Q2T;
    }
}
