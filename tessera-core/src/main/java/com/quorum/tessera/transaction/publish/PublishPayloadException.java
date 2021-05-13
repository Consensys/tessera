package com.quorum.tessera.transaction.publish;

/** An exception thrown when the target of a publish payload operation encounters an error */
public class PublishPayloadException extends RuntimeException {

  public PublishPayloadException(final String message) {
    super(message);
  }
}
