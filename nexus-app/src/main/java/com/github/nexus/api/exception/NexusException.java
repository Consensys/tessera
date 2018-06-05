package com.github.nexus.api.exception;


public abstract class NexusException extends RuntimeException {

    public NexusException(String message) {
        super(message);
    }

    public NexusException(String message, Throwable cause) {
        super(message, cause);
    }

    public NexusException(Throwable cause) {
        super(cause);
    }
    
}
