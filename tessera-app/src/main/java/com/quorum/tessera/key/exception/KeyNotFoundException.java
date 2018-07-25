package com.quorum.tessera.key.exception;

import com.quorum.tessera.exception.TesseraException;

/**
 * An exception thrown when a key is searched for but is not managed by this node
 */
public class KeyNotFoundException extends TesseraException {

    public KeyNotFoundException(final String message) {
        super(message);
    }

}
