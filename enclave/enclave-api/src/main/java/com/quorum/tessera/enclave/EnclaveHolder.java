package com.quorum.tessera.enclave;

import com.quorum.tessera.ServiceLoaderUtil;

import java.util.Optional;

public interface EnclaveHolder {

    Optional<Enclave> getEnclave();

    Enclave setEnclave(Enclave enclave);

    static EnclaveHolder getInstance() {
        return ServiceLoaderUtil.load(EnclaveHolder.class)
            .orElse(DefaultEnclaveHolder.INSTANCE);
    }

}
