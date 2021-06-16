package com.quorum.tessera.enclave;

import java.util.Optional;

public interface EnclaveHolder {

  Optional<Enclave> getEnclave();

  Enclave setEnclave(Enclave enclave);
}
