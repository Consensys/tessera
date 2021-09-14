package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class EnhancedPrivacyNotSupportedExceptionMapperTest {

  private EnhancedPrivacyNotSupportedExceptionMapper mapper =
      new EnhancedPrivacyNotSupportedExceptionMapper();

  @Test
  public void handleException() {

    final String message = ".. all outta gum";
    final EnhancedPrivacyNotSupportedException exception =
        new EnhancedPrivacyNotSupportedException(message);

    final Response result = mapper.toResponse(exception);

    assertThat(result.getStatus()).isEqualTo(403);
    assertThat(result.getEntity()).isEqualTo(message);
  }
}
