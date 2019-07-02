package com.quorum.tessera.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveNotAvailableExceptionMapper.class);

    @Override
    public Response toResponse(final EntityNotFoundException ex) {
        LOGGER.error("Entity not found: {}", ex.getMessage());
        LOGGER.debug(null, ex);

        return Response.status(Response.Status.NOT_FOUND)
            .entity(ex.getMessage())
            .type(MediaType.TEXT_PLAIN)
            .build();
    }

}
