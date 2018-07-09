package com.github.tessera.api;

import com.github.tessera.api.filter.GlobalFilter;
import com.github.tessera.api.filter.Logged;
import com.github.tessera.service.locator.ServiceLocator;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Logged
@GlobalFilter
@ApplicationPath("/")
public class Nexus extends Application {

    private final ServiceLocator serviceLocator;

    private final String contextName;

    public Nexus(final ServiceLocator serviceLocator, final String contextName) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
        this.contextName = Objects.requireNonNull(contextName);
    }

    @Override
    public Set<Object> getSingletons() {
        String apiPackageName = getClass().getPackage().getName();
        return serviceLocator.getServices(contextName).stream()
            .filter(Objects::nonNull)
            .filter(o -> Objects.nonNull(o.getClass()))
            .filter(o -> Objects.nonNull(o.getClass().getPackage()))
            .filter(o -> o.getClass().getPackage().getName().startsWith(apiPackageName))
            .collect(Collectors.toSet());
    }

}
