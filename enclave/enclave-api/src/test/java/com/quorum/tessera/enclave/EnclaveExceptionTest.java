package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EnclaveExceptionTest {

  @Test
  public void createWithMessage() {
    final EnclaveException enclaveException = new EnclaveException("Ouch");

    assertThat(enclaveException.getCause()).isNull();
    assertThat(enclaveException.getMessage()).isEqualTo("Ouch");
  }
}
