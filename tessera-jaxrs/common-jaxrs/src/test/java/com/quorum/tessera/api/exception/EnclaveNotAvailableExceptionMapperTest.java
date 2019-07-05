package com.quorum.tessera.api.exception;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class EnclaveNotAvailableExceptionMapperTest {

    private EnclaveNotAvailableExceptionMapper instance = new EnclaveNotAvailableExceptionMapper();

    @Test
    public void toResponse() {
        final EnclaveNotAvailableException exception = new EnclaveNotAvailableException("Enclave error");

        final Response result = instance.toResponse(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(503);
        assertThat(result.getEntity()).isEqualTo(exception.getMessage());
    }
}
