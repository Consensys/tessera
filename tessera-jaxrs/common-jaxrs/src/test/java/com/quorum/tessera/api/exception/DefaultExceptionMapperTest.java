package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class DefaultExceptionMapperTest {

  private DefaultExceptionMapper exceptionMapper = new DefaultExceptionMapper();

  @Test
  public void toResponse() {
    final Throwable exception = new Exception("Ouch");

    final Response result = exceptionMapper.toResponse(exception);

    assertThat(result.getStatus()).isEqualTo(500);
    assertThat(result.getEntity()).isEqualTo("Ouch");
  }

  @Test
  public void toResponseNestedCause() {
    final Throwable nested = new Exception("Ouch");
    final Throwable exception = new Exception(nested);

    final Response result = exceptionMapper.toResponse(exception);

    assertThat(result.getStatus()).isEqualTo(500);
    assertThat(result.getEntity()).isEqualTo("Ouch");
  }
}
