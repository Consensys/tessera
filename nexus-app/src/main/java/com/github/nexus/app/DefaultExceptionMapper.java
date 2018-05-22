package com.github.nexus.app;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger LOGGER = Logger.getLogger(DefaultExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(Exception exception) {
        
        LOGGER.log(Level.SEVERE, "",exception);
        
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage())
                .build();
    } 
}
