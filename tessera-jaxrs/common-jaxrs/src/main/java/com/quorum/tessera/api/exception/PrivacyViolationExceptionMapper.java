package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class PrivacyViolationExceptionMapper implements ExceptionMapper<PrivacyViolationException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PrivacyViolationExceptionMapper.class);

  @Override
  public Response toResponse(final PrivacyViolationException exception) {
    LOGGER.debug(null, exception);

    return Response.status(Response.Status.FORBIDDEN)
        .entity(exception.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
