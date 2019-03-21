package com.quorum.tessera.enclave.rest;

import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class DefaultExceptionMapperTest {

    private DefaultExceptionMapper exceptionMapper = new DefaultExceptionMapper();

    @Test
    public void toResponse() throws Exception {

        Throwable exception = new Exception("Ouch");

        Response result = exceptionMapper.toResponse(exception);
        assertThat(result.getStatus())
            .isEqualTo(500);

        assertThat(result.getStatusInfo().getReasonPhrase())
            .isEqualTo("Ouch");

    }
    
        @Test
    public void toResponseNestedCause() throws Exception {

        Throwable nested = new Exception("Ouch");
        Throwable exception = new Exception(nested);
        Response result = exceptionMapper.toResponse(exception);
        assertThat(result.getStatus())
            .isEqualTo(500);

        assertThat(result.getStatusInfo().getReasonPhrase())
            .isEqualTo("Ouch");

    }

}
