package com.quorum.tessera.transaction.exception;

/**
 * Thrown when a key wasn't found during a decryption operation
 */
public class KeyNotFoundException extends RuntimeException {

    public KeyNotFoundException(final String message) {
        super(message);
    }

}
