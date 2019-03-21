package com.quorum.tessera.enclave;

public class EnclaveException extends RuntimeException {

    public EnclaveException(String message) {
        super(message);
    }

    public EnclaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnclaveException(Throwable cause) {
        super(cause);
    }
    
}
