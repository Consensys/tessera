package com.quorum.tessera.api.exception;

import com.quorum.tessera.encryption.KeyNotFoundException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyNotFoundExceptionMapperTest {

    private KeyNotFoundExceptionMapper instance = new KeyNotFoundExceptionMapper();

    @Test
    public void toResponse() {
        final KeyNotFoundException keyNotFoundException = new KeyNotFoundException("OUCH");

        final Response result = instance.toResponse(keyNotFoundException);
        assertThat(result).isNotNull();

        final String message = (String) result.getEntity();

        assertThat(message).isEqualTo("OUCH");
        assertThat(result.getStatus()).isEqualTo(404);
    }
}
