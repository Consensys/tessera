package com.quorum.tessera.api.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityExceptionMapper.class);

  @Override
  public Response toResponse(final SecurityException exception) {
    LOGGER.error("Security exception", exception);

    // Return 500 assume access attempt is malicious
    return Response.serverError().build();
  }
}
