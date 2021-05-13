package com.quorum.tessera.encryption;

/** An exception thrown when a key is searched for but is not managed by this node */
public class KeyNotFoundException extends RuntimeException {

  public KeyNotFoundException(final String message) {
    super(message);
  }
}
