package com.quorum.tessera.api.exception;

import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotSupportedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class PrivacyGroupNotSupportedExceptionMapper
    implements ExceptionMapper<PrivacyGroupNotSupportedException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(PrivacyGroupNotSupportedExceptionMapper.class);

  @Override
  public Response toResponse(final PrivacyGroupNotSupportedException exception) {
    LOGGER.debug(null, exception);

    return Response.status(Response.Status.FORBIDDEN)
        .entity(exception.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
