package com.quorum.tessera.api.exception;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EnclaveNotAvailableExceptionMapper implements ExceptionMapper<EnclaveNotAvailableException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveNotAvailableExceptionMapper.class);

    @Override
    public Response toResponse(final EnclaveNotAvailableException ex) {
        LOGGER.error("Enclave unavailable: {}", ex.getMessage());
        LOGGER.debug(null, ex);

        return Response.status(Status.SERVICE_UNAVAILABLE)
            .entity(ex.getMessage())
            .type(MediaType.TEXT_PLAIN)
            .build();
    }

}
