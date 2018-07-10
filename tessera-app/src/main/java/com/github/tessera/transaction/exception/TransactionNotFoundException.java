package com.github.tessera.transaction.exception;

import com.github.tessera.exception.TesseraException;

public class TransactionNotFoundException extends TesseraException {

    public TransactionNotFoundException(final String message) {
        super(message);
    }

}
