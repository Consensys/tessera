package com.quorum.tessera.enclave;

/**
 * Thrown when the {@link Enclave} was requested to perform an operation but
 * was unavailable.
 */
public class EnclaveNotAvailableException extends RuntimeException {

    public EnclaveNotAvailableException() {
        super("Enclave service is not accessible");
    }

}
