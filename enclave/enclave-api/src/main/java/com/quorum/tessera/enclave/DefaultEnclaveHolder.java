package com.quorum.tessera.enclave;

import java.util.Optional;

enum DefaultEnclaveHolder implements EnclaveHolder {
  INSTANCE;

  private Enclave enclave;

  @Override
  public Optional<Enclave> getEnclave() {
    return Optional.ofNullable(enclave);
  }

  @Override
  public Enclave setEnclave(Enclave enclave) {
    if (this.enclave != null) throw new IllegalArgumentException("Enclave already stored");
    this.enclave = enclave;
    return enclave;
  }

  void reset() {
    enclave = null;
  }
}
