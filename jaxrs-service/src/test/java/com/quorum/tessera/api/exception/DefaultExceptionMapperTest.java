
package com.quorum.tessera.api.exception;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultExceptionMapperTest {

    private DefaultExceptionMapper instance = new DefaultExceptionMapper();

    @Test
    public void toResponse() {

        final String message = "OUCH That's gotta smart!!";

        Exception exception = new Exception(message);

        Response result = instance.toResponse(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getEntity()).isEqualTo(message);

    }
}
