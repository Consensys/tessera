package com.quorum.tessera.enclave.rest;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.Application;

public class EnclaveApplication extends Application implements com.quorum.tessera.config.apps.EnclaveApp {

    private final EnclaveResource resource;

    public EnclaveApplication(EnclaveResource resource) {
        this.resource = Objects.requireNonNull(resource);
    }

    @Override
    public Set<Object> getSingletons() {
        return Stream.of(resource,new DefaultExceptionMapper())
            .collect(Collectors.toSet());
    }

}
