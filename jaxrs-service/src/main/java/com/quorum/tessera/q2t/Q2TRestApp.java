package com.quorum.tessera.q2t;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.app.RestApp;
import com.quorum.tessera.config.apps.Q2TApp;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server
 * Contains all the service classes created by the service locator
 */
@GlobalFilter
@ApplicationPath("/")
public class Q2TRestApp extends RestApp implements Q2TApp {

    public Q2TRestApp(final ServiceLocator serviceLocator, final String contextName) {
        super(serviceLocator, contextName);
    }
}
