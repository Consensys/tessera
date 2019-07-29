package com.quorum.tessera.grpc;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.service.locator.ServiceLocator;
import io.grpc.BindableService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class GrpcApp implements TesseraApp {

    private final ServiceLocator serviceLocator;

    public GrpcApp(final ServiceLocator serviceLocator) {
        this.serviceLocator = Objects.requireNonNull(serviceLocator);
    }

    public Set<BindableService> getBindableServices() {
        final String apiPackageName = getClass().getPackage().getName();
        return serviceLocator.getServices().stream()
                .filter(Objects::nonNull)
                .filter(BindableService.class::isInstance)
                .filter(o -> Objects.nonNull(o.getClass()))
                .filter(o -> Objects.nonNull(o.getClass().getPackage()))
                .filter(o -> o.getClass().getPackage().getName().startsWith(apiPackageName))
                .map(o -> (BindableService) o)
                .collect(Collectors.toSet());
    }

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.GRPC;
    }
}
