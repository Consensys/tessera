package com.quorum.tessera.api.exception;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.xml.bind.UnmarshalException;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    @Override
    public Response toResponse(final WebApplicationException exception) {

        Throwable cause = Optional.ofNullable(exception.getCause()).orElse(exception);

        LOGGER.debug("Exception ", exception);
        LOGGER.debug("Root cause ", cause);
        final Response.Status returnStatus =
                UnmarshalException.class.isInstance(cause)
                        ? Response.Status.BAD_REQUEST
                        : Response.Status.INTERNAL_SERVER_ERROR;

        return Response.status(returnStatus).entity(cause.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
}
