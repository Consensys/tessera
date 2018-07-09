package com.github.tessera.exception;


public abstract class TesseraException extends RuntimeException {

    public TesseraException(String message) {
        super(message);
    }

    public TesseraException(String message, Throwable cause) {
        super(message, cause);
    }

    public TesseraException(Throwable cause) {
        super(cause);
    }
    
}
