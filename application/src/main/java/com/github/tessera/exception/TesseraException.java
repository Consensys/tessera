package com.github.tessera.exception;

public abstract class TesseraException extends RuntimeException {

    public TesseraException(final String message) {
        super(message);
    }

    public TesseraException(final Throwable cause) {
        super(cause);
    }
    
}
