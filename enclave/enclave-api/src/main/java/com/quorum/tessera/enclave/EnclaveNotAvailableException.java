package com.quorum.tessera.enclave;

/** Thrown when the {@link Enclave} was requested to perform an operation but was unavailable. */
public class EnclaveNotAvailableException extends EnclaveException {

  public EnclaveNotAvailableException() {
    super("Enclave service is not accessible");
  }

  public EnclaveNotAvailableException(String message) {
    super(message);
  }
}
