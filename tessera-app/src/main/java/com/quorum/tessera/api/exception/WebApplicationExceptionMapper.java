package com.quorum.tessera.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.xml.bind.UnmarshalException;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(final WebApplicationException exception) {

        LOGGER.debug("{}", exception.getClass());
        LOGGER.debug("{}", exception.getCause() == null ? "No cause" : exception.getCause().getClass());

        LOGGER.error("{}", exception.getMessage());

        try {
            throw exception.getCause();

        } catch (final UnmarshalException ex) {
            LOGGER.warn("Unable to unmarshal payload");
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();

        } catch (final Throwable ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();

        }

    }

}
