package com.quorum.tessera.enclave;

import com.quorum.tessera.service.Service;

import java.util.ServiceLoader;

/**
 * A client which interfaces with a remote {@link Enclave} over a defined
 * transport mechanism.
 */
public interface EnclaveClient extends Enclave {

    default void validateEnclaveStatus() {
        if (status() == Service.Status.STOPPED) {
            throw new EnclaveNotAvailableException();
        }
    }

    static EnclaveClient create() {
        return ServiceLoader.load(EnclaveClient.class).findFirst().get();
    }

}
