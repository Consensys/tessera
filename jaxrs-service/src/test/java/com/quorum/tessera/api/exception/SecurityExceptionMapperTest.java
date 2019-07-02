package com.quorum.tessera.api.exception;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityExceptionMapperTest {

    private SecurityExceptionMapper instance = new SecurityExceptionMapper();

    @Test
    public void toResponse() {
        final SecurityException securityException = new SecurityException("OUCH");

        final Response result = instance.toResponse(securityException);

        assertThat(result).isNotNull();
        assertThat(result.getEntity()).isNull();
        assertThat(result.getStatus()).isEqualTo(500);
    }
}
