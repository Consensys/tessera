package com.quorum.tessera.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);
    
    @Override
    public Response toResponse(Exception exception) {
        
        LOGGER.error("{}",exception.getMessage());
        
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage())
                .build();
    }
}
