package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.base64.DecodingException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class DecodingExceptionMapperTest {

  private DecodingExceptionMapper instance = new DecodingExceptionMapper();

  @Test
  public void toResponse() {
    final Throwable cause = new Exception("OUCH");
    final DecodingException decodingException = new DecodingException(cause);

    final Response result = instance.toResponse(decodingException);

    assertThat(result).isNotNull();

    final String message = result.getEntity().toString();

    assertThat(message).isEqualTo("java.lang.Exception: OUCH");
    assertThat(result.getStatus()).isEqualTo(400);
  }
}
