package com.quorum.tessera.nacl;

/**
 * An exception type that is when a generic exception occurring with key
 * processing
 *
 * If a more specific exception type exists when processing a key than that
 * should be used instead
 */
public class KeyException extends RuntimeException {

    public KeyException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
