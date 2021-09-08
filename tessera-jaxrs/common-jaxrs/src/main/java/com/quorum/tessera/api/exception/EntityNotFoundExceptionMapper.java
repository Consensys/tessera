package com.quorum.tessera.api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityNotFoundExceptionMapper.class);

  @Override
  public Response toResponse(final EntityNotFoundException ex) {
    LOGGER.warn("Entity not found: {}", ex.getMessage());
    LOGGER.debug(null, ex);

    return Response.status(Response.Status.NOT_FOUND)
        .entity(ex.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
