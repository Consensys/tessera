package com.github.nexus.util.exception;

import com.github.nexus.exception.NexusException;

public class DecodingException extends NexusException {

    public DecodingException(String message) {
        super(message);
    }

    public DecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodingException(Throwable cause) {
        super(cause);
    }
}
