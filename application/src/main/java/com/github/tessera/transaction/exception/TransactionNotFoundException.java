package com.github.tessera.transaction.exception;

import com.github.tessera.exception.TesseraException;

public class TransactionNotFoundException extends TesseraException {

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionNotFoundException(Throwable cause) {
        super(cause);
    }
}
