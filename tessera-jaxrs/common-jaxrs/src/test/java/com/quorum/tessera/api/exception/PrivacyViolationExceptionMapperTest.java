package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyViolationExceptionMapperTest {

    private PrivacyViolationExceptionMapper mapper = new PrivacyViolationExceptionMapper();

    @Test
    public void handleException() {

        final String message = ".. all outta gum";
        final PrivacyViolationException exception = new PrivacyViolationException(message);

        final Response result = mapper.toResponse(exception);

        assertThat(result.getStatus()).isEqualTo(403);
        assertThat(result.getEntity()).isEqualTo(message);
    }
}
