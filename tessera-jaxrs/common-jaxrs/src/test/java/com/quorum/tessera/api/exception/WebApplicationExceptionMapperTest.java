package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.UnmarshalException;
import org.junit.Test;

public class WebApplicationExceptionMapperTest {

  private WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();

  @Test
  public void nullCauseGets500() {
    final WebApplicationException exception = new WebApplicationException("NullPointer message");

    final Response response = mapper.toResponse(exception);

    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  public void otherCauseGets500() {
    final RuntimeException cause = new RuntimeException("OtherMessage message");
    final WebApplicationException exception = new WebApplicationException(cause);

    final Response response = mapper.toResponse(exception);

    assertThat(response.getStatus()).isEqualTo(500);
  }

  @Test
  public void unmarshalExceptionCauseGets400() {
    final UnmarshalException cause = new UnmarshalException("Unmarshal message");
    final WebApplicationException exception = new WebApplicationException(cause);

    final Response response = mapper.toResponse(exception);

    assertThat(response.getStatus()).isEqualTo(400);
  }
}
