package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class EnclaveNotAvailableExceptionMapperTest {

  private EnclaveNotAvailableExceptionMapper instance = new EnclaveNotAvailableExceptionMapper();

  @Test
  public void toResponse() {
    final EnclaveNotAvailableException exception =
        new EnclaveNotAvailableException("Enclave error");

    final Response result = instance.toResponse(exception);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(503);
    assertThat(result.getEntity()).isEqualTo(exception.getMessage());
  }
}
