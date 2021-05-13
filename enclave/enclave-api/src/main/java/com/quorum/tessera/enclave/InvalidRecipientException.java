package com.quorum.tessera.enclave;

/** An exception to indicate the recipient is valid for the operation being performed */
public class InvalidRecipientException extends RuntimeException {

  public InvalidRecipientException(final String message) {
    super(message);
  }
}
