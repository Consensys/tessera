package com.quorum.tessera.socket;

/**
 * A generic exception for all socket related issues
 */
public class TesseraSocketException extends RuntimeException {

    public TesseraSocketException(final Throwable cause) {
        super(cause);
    }

}
