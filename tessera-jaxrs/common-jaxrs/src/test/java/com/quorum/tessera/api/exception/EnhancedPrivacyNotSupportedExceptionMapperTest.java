package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.EnhancedPrivacyNotSupportedException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class EnhancedPrivacyNotSupportedExceptionMapperTest {

    private EnhancedPrivacyNotSupportedExceptionMapper mapper = new EnhancedPrivacyNotSupportedExceptionMapper();

    @Test
    public void handleException() {

        final String message = ".. all outta gum";
        final EnhancedPrivacyNotSupportedException exception = new EnhancedPrivacyNotSupportedException(message);

        final Response result = mapper.toResponse(exception);

        assertThat(result.getStatus()).isEqualTo(403);
        assertThat(result.getEntity()).isEqualTo(message);
    }
}
