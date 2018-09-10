package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.GlobalFilter;
import com.quorum.tessera.api.filter.Logged;
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
@Logged
@GlobalFilter
@ApplicationPath("/")
public class Tessera extends Application {

    private final ServiceLocator serviceLocator;

    private final String contextName;
        
    public Tessera(final ServiceLocator serviceLocator, final String contextName) {
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
