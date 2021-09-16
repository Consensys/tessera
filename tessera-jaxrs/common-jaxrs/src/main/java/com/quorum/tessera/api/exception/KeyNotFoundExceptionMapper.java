package com.quorum.tessera.api.exception;

import com.quorum.tessera.encryption.KeyNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class KeyNotFoundExceptionMapper implements ExceptionMapper<KeyNotFoundException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyNotFoundExceptionMapper.class);

  @Override
  public Response toResponse(final KeyNotFoundException e) {
    LOGGER.warn(e.getMessage());
    LOGGER.debug(null, e);

    return Response.status(Response.Status.NOT_FOUND)
        .entity(e.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
