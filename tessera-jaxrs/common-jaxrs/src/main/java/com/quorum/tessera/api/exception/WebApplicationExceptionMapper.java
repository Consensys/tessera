package com.quorum.tessera.api.exception;

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
        LOGGER.debug("{}", exception.getClass());
        LOGGER.debug("{}", exception.getCause() == null ? "No cause" : exception.getCause().getClass());

        LOGGER.error("{}", exception.getMessage());

        final Response.Status returnStatus;
        try {
            throw exception.getCause();
        } catch (final UnmarshalException ex) {
            LOGGER.warn("Unable to unmarshal payload");
            returnStatus = Response.Status.BAD_REQUEST;
        } catch (final Throwable ex) {
            returnStatus = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(returnStatus).entity(exception.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
}
