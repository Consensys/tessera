package com.quorum.tessera.enclave;

/**
 * A generic exception when an {@link Enclave} encounters an error performing the requested
 * operation.
 */
public class EnclaveException extends RuntimeException {

  public EnclaveException(final String message) {
    super(message);
  }
}
