package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class EntityNotFoundExceptionMapperTest {

  private EntityNotFoundExceptionMapper instance = new EntityNotFoundExceptionMapper();

  @Test
  public void toResponse() {
    final String message = "OUCH That's gotta smart!!";

    final EntityNotFoundException exception = new EntityNotFoundException(message);

    final Response result = instance.toResponse(exception);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(404);
    assertThat(result.getEntity()).isEqualTo(message);
  }
}
