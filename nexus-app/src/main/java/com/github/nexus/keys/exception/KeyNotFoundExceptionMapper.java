package com.github.nexus.keys.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class KeyNotFoundExceptionMapper implements ExceptionMapper<KeyNotFoundException> {

    private static final Logger LOGGER = Logger.getLogger(KeyNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(KeyNotFoundException e) {
        LOGGER.log(Level.SEVERE, "",e);

        return Response.status(Response.Status.BAD_REQUEST)
            .entity(e.getMessage())
            .header("Content-Type", MediaType.TEXT_PLAIN)
            .build();
    }
}
