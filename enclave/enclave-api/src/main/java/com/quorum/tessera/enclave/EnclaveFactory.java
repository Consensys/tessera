package com.quorum.tessera.enclave;

import java.util.ServiceLoader;

public interface EnclaveFactory<C> {

    Enclave create(C config);
    
    static EnclaveFactory create() {
        return ServiceLoader.load(EnclaveFactory.class).iterator().next();
    }

}
