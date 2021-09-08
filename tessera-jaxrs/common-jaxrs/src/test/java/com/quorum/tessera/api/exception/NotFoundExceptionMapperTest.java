package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class NotFoundExceptionMapperTest {

  private NotFoundExceptionMapper mapper = new NotFoundExceptionMapper();

  @Test
  public void toResponse() {
    final String message = "What are you talking about Willis?!?";
    final NotFoundException exception = new NotFoundException(message);
    final Response response = mapper.toResponse(exception);

    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(response.getEntity()).isEqualTo(message);
  }
}
