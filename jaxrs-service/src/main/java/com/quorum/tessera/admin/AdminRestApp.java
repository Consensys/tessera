package com.quorum.tessera.admin;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.app.RestApp;
import com.quorum.tessera.config.apps.AdminApp;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;

/**
 * An app that allows access to node management resources
 */
@GlobalFilter
@ApplicationPath("/")
public class AdminRestApp extends RestApp implements AdminApp {

    public AdminRestApp(final ServiceLocator serviceLocator, final String contextName) {
        super(serviceLocator, contextName);
    }

}
