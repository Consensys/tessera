package com.github.nexus.transaction.exception;

import com.github.nexus.exception.NexusException;

public class TransactionNotFoundException extends NexusException {

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
