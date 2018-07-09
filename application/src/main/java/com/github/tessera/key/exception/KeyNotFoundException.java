package com.github.tessera.key.exception;

import com.github.tessera.exception.NexusException;

public class KeyNotFoundException extends NexusException {

    public KeyNotFoundException(final String message) {
        super(message);
    }

}
