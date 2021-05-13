package com.quorum.tessera.api.exception;

import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class PrivacyGroupNotFoundExceptionMapper
    implements ExceptionMapper<PrivacyGroupNotFoundException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PrivacyGroupNotFoundExceptionMapper.class);

  @Override
  public Response toResponse(final PrivacyGroupNotFoundException exception) {
    LOGGER.debug(null, exception);

    return Response.status(Response.Status.NOT_FOUND)
        .entity(exception.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
