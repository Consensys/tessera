
package com.quorum.tessera.enclave.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {
        
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);
    
    @Override
    public Response toResponse(Throwable ex) {
        
        LOGGER.debug(null, ex);
        
        Throwable cause = findCause(ex);

        return Response.status(500, cause.getMessage())
            .type(MediaType.TEXT_PLAIN_TYPE)
            .build();
    }
    
    private static Throwable findCause(Throwable ex) {
        if(ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }
    
}
