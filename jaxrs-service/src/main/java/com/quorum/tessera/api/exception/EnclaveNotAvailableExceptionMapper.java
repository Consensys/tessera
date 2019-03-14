package com.quorum.tessera.api.exception;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EnclaveNotAvailableExceptionMapper implements ExceptionMapper<EnclaveNotAvailableException> {

    @Override
    public Response toResponse(EnclaveNotAvailableException e) {
        return Response.status(Status.SERVICE_UNAVAILABLE)
                .entity(e.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
    
}
