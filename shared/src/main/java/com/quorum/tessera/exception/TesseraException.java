package com.quorum.tessera.exception;

/**
 * A generic exception that all other exception types should extend that deal with application
 * failures
 */
public abstract class TesseraException extends RuntimeException {

  public TesseraException(final String message) {
    super(message);
  }

  public TesseraException(final Throwable cause) {
    super(cause);
  }
}
