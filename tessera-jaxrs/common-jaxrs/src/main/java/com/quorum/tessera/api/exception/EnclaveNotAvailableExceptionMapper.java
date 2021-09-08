package com.quorum.tessera.api.exception;

import com.quorum.tessera.enclave.EnclaveNotAvailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class EnclaveNotAvailableExceptionMapper
    implements ExceptionMapper<EnclaveNotAvailableException> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(EnclaveNotAvailableExceptionMapper.class);

  @Override
  public Response toResponse(final EnclaveNotAvailableException ex) {
    LOGGER.error("Enclave unavailable: {}", ex.getMessage());
    LOGGER.debug(null, ex);

    return Response.status(Response.Status.SERVICE_UNAVAILABLE)
        .entity(ex.getMessage())
        .type(MediaType.TEXT_PLAIN)
        .build();
  }
}
