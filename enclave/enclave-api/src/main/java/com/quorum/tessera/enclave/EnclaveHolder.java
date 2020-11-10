package com.quorum.tessera.enclave;

import java.util.Optional;
import java.util.ServiceLoader;

public interface EnclaveHolder {

    Optional<Enclave> getEnclave();

    Enclave setEnclave(Enclave enclave);

    static EnclaveHolder getInstance() {
        return ServiceLoader.load(EnclaveHolder.class).findFirst()
            .orElse(DefaultEnclaveHolder.INSTANCE);
    }

}
