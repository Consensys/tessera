package com.quorum.tessera.api.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotFoundExceptionMapper.class);

  @Override
  public Response toResponse(final NotFoundException ex) {
    LOGGER.warn("Entity not found: {}", ex.getMessage());
    LOGGER.debug(null, ex);

    return Response.status(Status.NOT_FOUND)
        .entity(ex.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
