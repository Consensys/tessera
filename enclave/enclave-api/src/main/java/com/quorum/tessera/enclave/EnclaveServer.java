package com.quorum.tessera.enclave;

import com.quorum.tessera.service.Service;

import java.util.ServiceLoader;

public interface EnclaveServer extends Enclave, Service {

    static EnclaveServer create() {
        return ServiceLoader.load(EnclaveServer.class).findFirst().get();
    }

}
