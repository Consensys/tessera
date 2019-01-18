package com.quorum.tessera.enclave.rest;

import java.util.Collections;
import java.util.Set;
import javax.ws.rs.core.Application;

public class EnclaveApplication extends Application implements com.quorum.tessera.config.apps.EnclaveApp {

    private final EnclaveResource resource;

    public EnclaveApplication(EnclaveResource resource) {
        this.resource = resource;
    }

    @Override
    public Set<Object> getSingletons() {
        return Collections.singleton(resource);
    }

}
