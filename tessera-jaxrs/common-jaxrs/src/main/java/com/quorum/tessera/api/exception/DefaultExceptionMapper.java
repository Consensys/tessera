package com.quorum.tessera.api.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

  @Override
  public Response toResponse(final Throwable ex) {
    final Throwable rootCause = ExceptionUtils.getRootCause(ex);
    final Throwable cause = (rootCause == null) ? ex : rootCause;

    LOGGER.error("Error occurred: {}. Root cause: {}", ex.getMessage(), cause.getMessage());
    LOGGER.debug(null, ex);
    LOGGER.debug(null, cause);

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(cause.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
