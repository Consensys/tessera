package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;


public class EnclaveNotAvailableException extends TesseraException {
    
    public EnclaveNotAvailableException() {
        super("Enclave service is not accessible");
    }
    
}
