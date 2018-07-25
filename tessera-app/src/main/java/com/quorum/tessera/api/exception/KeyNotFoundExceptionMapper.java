package com.quorum.tessera.api.exception;

import com.quorum.tessera.key.exception.KeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class KeyNotFoundExceptionMapper implements ExceptionMapper<KeyNotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyNotFoundExceptionMapper.class);

    @Override
    public Response toResponse(final KeyNotFoundException e) {
        LOGGER.error("", e);

        //TODO: change to 404
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(e.getMessage())
            .header("Content-Type", MediaType.TEXT_PLAIN)
            .build();
    }
}
