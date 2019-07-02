package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import javax.ws.rs.core.Application;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnclaveApplication extends Application implements com.quorum.tessera.config.apps.TesseraApp {

    private final EnclaveResource resource;

    public EnclaveApplication(final EnclaveResource resource) {
        this.resource = Objects.requireNonNull(resource);
    }

    @Override
    public Set<Object> getSingletons() {
        return Stream.of(resource, new DefaultExceptionMapper()).collect(Collectors.toSet());
    }

    @Override
    public AppType getAppType() {
        return AppType.ENCLAVE;
    }
}
