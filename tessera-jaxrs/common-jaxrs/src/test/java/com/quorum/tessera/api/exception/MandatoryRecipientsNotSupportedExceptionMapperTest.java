package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotSupportedException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MandatoryRecipientsNotSupportedExceptionMapperTest {

  private MandatoryRecipientsNotSupportedExceptionMapper instance = new MandatoryRecipientsNotSupportedExceptionMapper();

  @Test
  public void toResponse() {
    final MandatoryRecipientsNotSupportedException ex = new MandatoryRecipientsNotSupportedException("OUCH");

    final Response result = instance.toResponse(ex);
    assertThat(result).isNotNull();

    final String message = (String) result.getEntity();

    assertThat(message).isEqualTo("OUCH");
    assertThat(result.getStatus()).isEqualTo(403);
  }
}
