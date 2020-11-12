package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;

import javax.ws.rs.core.Application;
import java.util.Set;

public class EnclaveApplication extends Application implements com.quorum.tessera.config.apps.TesseraApp {


    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(EnclaveResource.class,DefaultExceptionMapper.class);
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
