package com.quorum.tessera.nacl;

/**
 * An exception to be thrown when the underlying implementation library returns an
 * error (either it throws an exception or returns an error code)
 */
public class NaclException extends RuntimeException {

    public NaclException(final String message) {
        super(message);
    }

}
