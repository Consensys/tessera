package com.quorum.tessera.api.exception;

import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupNotSupportedExceptionMapperTest {

    private PrivacyGroupNotSupportedExceptionMapper mapper = new PrivacyGroupNotSupportedExceptionMapper();

    @Test
    public void handleException() {

        final String message = ".. all outta gum";
        final PrivacyGroupNotSupportedException exception = new PrivacyGroupNotSupportedException(message);

        final Response result = mapper.toResponse(exception);

        assertThat(result.getStatus()).isEqualTo(403);
        assertThat(result.getEntity()).isEqualTo(message);
    }
}
