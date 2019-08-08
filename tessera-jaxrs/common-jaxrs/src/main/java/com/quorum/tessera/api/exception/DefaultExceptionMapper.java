package com.quorum.tessera.api.exception;

import com.quorum.tessera.exception.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(final Throwable ex) {

        final Throwable cause = ExceptionUtil.extractCause(ex);

        LOGGER.error("Error occured: {}. Root cause: {}", ex.getMessage(), cause.getMessage());
        LOGGER.debug("Exception thrown", ex);
        LOGGER.debug("Cause of exception", cause);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(cause.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
