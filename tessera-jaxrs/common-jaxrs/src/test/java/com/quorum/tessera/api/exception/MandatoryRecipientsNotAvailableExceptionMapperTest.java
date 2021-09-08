package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotAvailableException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MandatoryRecipientsNotAvailableExceptionMapperTest {

  private MandatoryRecipientsNotAvailableExceptionMapper instance = new MandatoryRecipientsNotAvailableExceptionMapper();

  @Test
  public void toResponse() {
    final MandatoryRecipientsNotAvailableException ex = new MandatoryRecipientsNotAvailableException("OUCH");

    final Response result = instance.toResponse(ex);
    assertThat(result).isNotNull();

    final String message = (String) result.getEntity();

    assertThat(message).isEqualTo("OUCH");
    assertThat(result.getStatus()).isEqualTo(400);
  }
}
