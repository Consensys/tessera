package com.quorum.tessera.api.exception;

import com.quorum.tessera.util.exception.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DecodingExceptionMapper implements ExceptionMapper<DecodingException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecodingExceptionMapper.class);

    @Override
    public Response toResponse(final DecodingException exception) {
        LOGGER.warn("Failed to decode message: {}", exception.getMessage());
        LOGGER.debug(null, exception);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
