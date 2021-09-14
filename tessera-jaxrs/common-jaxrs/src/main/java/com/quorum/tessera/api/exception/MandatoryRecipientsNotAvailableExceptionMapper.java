package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.MandatoryRecipientsNotAvailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class MandatoryRecipientsNotAvailableExceptionMapper
    implements ExceptionMapper<MandatoryRecipientsNotAvailableException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MandatoryRecipientsNotAvailableExceptionMapper.class);

  @Override
  public Response toResponse(final MandatoryRecipientsNotAvailableException exception) {
    LOGGER.debug(null, exception);

    return Response.status(Response.Status.BAD_REQUEST)
        .entity(exception.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
