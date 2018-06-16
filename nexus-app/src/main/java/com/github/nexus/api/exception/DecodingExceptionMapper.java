package com.github.nexus.api.exception;

import com.github.nexus.util.exception.DecodingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class DecodingExceptionMapper implements ExceptionMapper<DecodingException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecodingExceptionMapper.class);

    @Override
    public Response toResponse(DecodingException e) {
        LOGGER.error("",e);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .header("Content-Type", MediaType.TEXT_PLAIN)
                .build();
    }
}
