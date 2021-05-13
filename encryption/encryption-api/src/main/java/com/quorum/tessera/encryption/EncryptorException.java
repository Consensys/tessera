package com.quorum.tessera.encryption;

/**
 * An exception to be thrown when the underlying implementation library returns an error (either it
 * throws an exception or returns an error code)
 */
public class EncryptorException extends RuntimeException {

  public EncryptorException(final String message) {
    super(message);
  }
}
