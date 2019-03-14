package com.quorum.tessera.enclave;


public class EnclaveNotAvailableException extends RuntimeException {
    public EnclaveNotAvailableException() {
        super("Enclave service is not accessible");
    }
}
