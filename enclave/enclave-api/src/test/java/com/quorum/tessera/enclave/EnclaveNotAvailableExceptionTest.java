package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EnclaveNotAvailableExceptionTest {

  @Test
  public void createWithMessage() {
    final EnclaveNotAvailableException enclaveException = new EnclaveNotAvailableException("Ouch");

    assertThat(enclaveException.getCause()).isNull();
    assertThat(enclaveException.getMessage()).isEqualTo("Ouch");
  }
}
