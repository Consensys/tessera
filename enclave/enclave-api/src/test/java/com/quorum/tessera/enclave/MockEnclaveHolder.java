package com.quorum.tessera.enclave;

import java.util.Optional;

public class MockEnclaveHolder implements EnclaveHolder {

  private static Enclave enc;

  @Override
  public Optional<Enclave> getEnclave() {
    return Optional.ofNullable(enc);
  }

  static void setMockEnclave(Enclave enclave) {
    enc = enclave;
  }

  static void reset() {
    enc = null;
  }

  @Override
  public Enclave setEnclave(Enclave enclave) {
    enc = enclave;
    return enc;
  }
}
