package com.quorum.tessera.thridparty;

import com.quorum.tessera.config.appmarkers.ThirdPartyAPP;
import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main application that is submitted to the HTTP server
 * Contains all the service classes created by the service locator
 */
@ApplicationPath("/")
public class ThirdParty extends Application implements ThirdPartyAPP {

    private final ServiceLocator serviceLocator;

    private final String contextName;

    public ThirdParty(final ServiceLocator serviceLocator, final String contextName) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
        this.contextName = Objects.requireNonNull(contextName);
    }

    @Override
    public Set<Object> getSingletons() {
        final String apiPackageName = getClass().getPackage().getName();

        return serviceLocator.getServices(contextName).stream()
            .filter(Objects::nonNull)
            .filter(o -> Objects.nonNull(o.getClass()))
            .filter(o -> Objects.nonNull(o.getClass().getPackage()))
            .filter(o -> o.getClass().getPackage().getName().startsWith(apiPackageName))
            .collect(Collectors.toSet());
    }

}
