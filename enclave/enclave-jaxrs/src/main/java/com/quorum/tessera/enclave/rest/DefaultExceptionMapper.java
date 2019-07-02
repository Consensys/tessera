package com.quorum.tessera.enclave.rest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable ex) {
        final Throwable rootCause = ExceptionUtils.getRootCause(ex);
        final Throwable cause = (rootCause == null) ? ex : rootCause;

        LOGGER.error("Error occured: {}. Root cause: {}", ex.getMessage(), cause.getMessage());
        LOGGER.debug(null, ex);
        LOGGER.debug(null, cause);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(cause.getMessage())
            .type(MediaType.TEXT_PLAIN)
            .build();
    }

}
