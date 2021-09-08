package com.quorum.tessera.api.exception;
import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MandatoryRecipientsNotSupportedExceptionMapper implements ExceptionMapper<MandatoryRecipientsNotSupportedException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MandatoryRecipientsNotSupportedExceptionMapper.class);

  @Override
  public Response toResponse(final MandatoryRecipientsNotSupportedException exception) {
    LOGGER.debug(null, exception);

    return Response.status(Response.Status.FORBIDDEN)
        .entity(exception.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
