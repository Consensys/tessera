package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.enclave.Enclave;

import javax.ws.rs.core.Application;
import java.util.Set;

public class EnclaveApplication extends Application implements com.quorum.tessera.config.apps.TesseraApp {

    private Enclave enclave;

    public EnclaveApplication(Enclave enclave) {
        this.enclave = enclave;
    }

    @Override
    public Set<Object> getSingletons() {
        return Set.of(new EnclaveResource(enclave),new DefaultExceptionMapper());
    }

    @Override
    public AppType getAppType() {
        return AppType.ENCLAVE;
    }

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.REST;
    }
}
