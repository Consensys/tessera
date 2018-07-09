
package com.github.tessera.api.exception;

import com.github.tessera.util.exception.DecodingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class DecodingExceptionMapperTest {

    private DecodingExceptionMapper instance = new DecodingExceptionMapper();

    @Test
    public void toResponse() {

        final Throwable cause = new Exception("OUCH");
        final DecodingException decodingException = new DecodingException(cause);

        final Response result = instance.toResponse(decodingException);

        assertThat(result).isNotNull();

        final String message = result.getEntity().toString();

        assertThat(message).isEqualTo("java.lang.Exception: OUCH");
        assertThat(result.getStatus()).isEqualTo(400);

    }

}
