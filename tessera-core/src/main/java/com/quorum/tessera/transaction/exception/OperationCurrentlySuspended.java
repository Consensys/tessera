package com.quorum.tessera.transaction.exception;

import com.quorum.tessera.exception.TesseraException;

public class OperationCurrentlySuspended extends TesseraException {
    public OperationCurrentlySuspended(String message) {
        super(message);
    }
}
