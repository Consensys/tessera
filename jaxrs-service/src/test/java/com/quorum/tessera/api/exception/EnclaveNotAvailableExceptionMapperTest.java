package com.quorum.tessera.api.exception;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


public class EnclaveNotAvailableExceptionMapperTest {
    
    private EnclaveNotAvailableExceptionMapper instance = new EnclaveNotAvailableExceptionMapper();

    @Test
    public void toResponse() {


        EnclaveNotAvailableException exception = new EnclaveNotAvailableException();

        Response result = instance.toResponse(exception);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(503);
        assertThat(result.getEntity()).isEqualTo(exception.getMessage());

    }
}
