package com.github.nexus.enclave.exception;

import com.github.nexus.api.exception.NexusException;

public class KeyNotFoundException extends NexusException {

    public KeyNotFoundException(String message) {
        super(message);
    }

    public KeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyNotFoundException(Throwable cause) {
        super(cause);
    }
}
