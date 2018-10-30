package com.quorum.tessera.thridparty;

import com.quorum.tessera.app.RestApp;
import com.quorum.tessera.config.apps.ThirdPartyApp;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;

/**
 * The third party API
 */
@ApplicationPath("/")
public class ThirdPartyRestApp extends RestApp implements ThirdPartyApp {

    public ThirdPartyRestApp(final ServiceLocator serviceLocator, final String contextName) {
        super(serviceLocator, contextName);
    }
}
