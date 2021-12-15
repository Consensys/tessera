package com.quorum.tessera.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import jakarta.ws.rs.core.Response;
import org.junit.Test;

public class PrivacyGroupNotSupportedExceptionMapperTest {

  private PrivacyGroupNotSupportedExceptionMapper mapper =
      new PrivacyGroupNotSupportedExceptionMapper();

  @Test
  public void handleException() {

    final String message = ".. all outta gum";
    final PrivacyGroupNotSupportedException exception =
        new PrivacyGroupNotSupportedException(message);

    final Response result = mapper.toResponse(exception);

    assertThat(result.getStatus()).isEqualTo(403);
    assertThat(result.getEntity()).isEqualTo(message);
  }
}
