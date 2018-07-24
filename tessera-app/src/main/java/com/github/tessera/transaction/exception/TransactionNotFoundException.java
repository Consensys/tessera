package com.github.tessera.transaction.exception;

import com.github.tessera.exception.TesseraException;

/**
 * An exception thrown when the transaction could not found in the underlying
 * data store
 */
public class TransactionNotFoundException extends TesseraException {

    public TransactionNotFoundException(final String message) {
        super(message);
    }

}
