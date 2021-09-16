package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class SecurityExceptionMapperTest {

  private SecurityExceptionMapper instance = new SecurityExceptionMapper();

  @Test
  public void toResponse() {
    final SecurityException securityException = new SecurityException("OUCH");

    final Response result = instance.toResponse(securityException);

    assertThat(result).isNotNull();
    assertThat(result.getEntity()).isNull();
    assertThat(result.getStatus()).isEqualTo(500);
  }
}
