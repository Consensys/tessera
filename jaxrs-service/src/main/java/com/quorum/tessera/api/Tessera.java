package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.app.RestApp;
import com.quorum.tessera.config.appmarkers.TesseraAPP;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;

/**
 * The main application that is submitted to the HTTP server
 * Contains all the service classes created by the service locator
 */
@GlobalFilter
@ApplicationPath("/")
public class Tessera extends RestApp implements TesseraAPP {

    public Tessera(final ServiceLocator serviceLocator, final String contextName) {
        super(serviceLocator, contextName);
    }
}
