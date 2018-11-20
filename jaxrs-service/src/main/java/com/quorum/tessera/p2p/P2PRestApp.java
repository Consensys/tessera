package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.app.RestApp;
import com.quorum.tessera.config.apps.P2PApp;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server
 * Contains all the service classes created by the service locator
 */
@GlobalFilter
@ApplicationPath("/")
public class P2PRestApp extends RestApp implements P2PApp {

    public P2PRestApp(final ServiceLocator serviceLocator, final String contextName) {
        super(serviceLocator, contextName);
    }
}
