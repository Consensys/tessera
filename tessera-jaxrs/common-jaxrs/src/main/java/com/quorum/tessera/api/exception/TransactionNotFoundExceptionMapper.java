package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class TransactionNotFoundExceptionMapper
    implements ExceptionMapper<TransactionNotFoundException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TransactionNotFoundExceptionMapper.class);

  @Override
  public Response toResponse(final TransactionNotFoundException e) {
    LOGGER.info(e.getMessage());

    return Response.status(Response.Status.NOT_FOUND)
        .entity(e.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
