package com.quorum.tessera.app;

import com.quorum.tessera.service.locator.ServiceLocator;

import javax.ws.rs.core.Application;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main application that is submitted to the HTTP server
 * Contains all the service classes created by the service locator
 */
public class RestApp extends Application {

    private final ServiceLocator serviceLocator;

    private final String contextName;

    public RestApp(final ServiceLocator serviceLocator, final String contextName) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
        this.contextName = Objects.requireNonNull(contextName);
    }

    @Override
    public Set<Object> getSingletons() {
        final String apiPackageName = getClass().getPackage().getName();

        //TODO find a nicer way for rest apps to share exception mappers / filters

        return serviceLocator.getServices(contextName).stream()
            .filter(Objects::nonNull)
            .filter(o -> Objects.nonNull(o.getClass()))
            .filter(o -> Objects.nonNull(o.getClass().getPackage()))
            .filter(o -> o.getClass().getPackage().getName().startsWith(apiPackageName) ||
                o.getClass().getPackage().getName().startsWith("com.quorum.tessera.api.exception") ||
                o.getClass().getPackage().getName().startsWith("com.quorum.tessera.api.filter") ||
                o.getClass().getPackage().getName().startsWith("com.quorum.tessera.api.common"))
            .collect(Collectors.toSet());
    }

}
