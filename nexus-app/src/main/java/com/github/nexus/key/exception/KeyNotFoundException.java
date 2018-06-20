package com.github.nexus.key.exception;

import com.github.nexus.exception.NexusException;

public class KeyNotFoundException extends NexusException {

    public KeyNotFoundException(final String message) {
        super(message);
    }

}
