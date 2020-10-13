package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PrivacyViolationExceptionMapper implements ExceptionMapper<PrivacyViolationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyViolationExceptionMapper.class);

    @Override
    public Response toResponse(final PrivacyViolationException exception) {
        LOGGER.debug(null, exception);

        return Response.status(Response.Status.FORBIDDEN)
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
