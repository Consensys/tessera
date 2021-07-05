package com.quorum.tessera.messaging;

public class CourierException extends RuntimeException {

  public CourierException(final String message) {
    super(message);
  }

  public CourierException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
