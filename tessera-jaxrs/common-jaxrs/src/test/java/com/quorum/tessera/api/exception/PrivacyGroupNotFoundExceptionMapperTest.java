package com.quorum.tessera.api.exception;

import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivacyGroupNotFoundExceptionMapperTest {

    private PrivacyGroupNotFoundExceptionMapper mapper = new PrivacyGroupNotFoundExceptionMapper();

    @Test
    public void handleException() {

        final String message = ".. all outta gum";
        final PrivacyGroupNotFoundException exception = new PrivacyGroupNotFoundException(message);

        final Response result = mapper.toResponse(exception);

        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getEntity()).isEqualTo(message);
    }
}
