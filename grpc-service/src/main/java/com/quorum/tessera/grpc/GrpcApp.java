package com.quorum.tessera.grpc;

import com.quorum.tessera.service.locator.ServiceLocator;
import io.grpc.BindableService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GrpcApp {
    private final ServiceLocator serviceLocator;

    private final String contextName;

    public GrpcApp(final ServiceLocator serviceLocator, final String contextName) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
        this.contextName = Objects.requireNonNull(contextName);
    }

    public Set<BindableService> getBindableServices() {
        final String apiPackageName = getClass().getPackage().getName();
        return serviceLocator.getServices(contextName).stream()
            .filter(Objects::nonNull)
            .filter(BindableService.class::isInstance)
            .filter(o -> Objects.nonNull(o.getClass()))
            .filter(o -> Objects.nonNull(o.getClass().getPackage()))
            .filter(o -> o.getClass().getPackage().getName().startsWith(apiPackageName))
            .map(o -> (BindableService) o)
            .collect(Collectors.toSet());
    }
}
