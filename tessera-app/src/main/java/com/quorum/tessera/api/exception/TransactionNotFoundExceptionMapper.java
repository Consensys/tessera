package com.quorum.tessera.api.exception;

import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class TransactionNotFoundExceptionMapper implements ExceptionMapper<TransactionNotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionNotFoundExceptionMapper.class);

    @Override
    public Response toResponse(final TransactionNotFoundException e) {
        LOGGER.info(e.getMessage());

        return Response.status(Response.Status.NOT_FOUND)
            .entity(e.getMessage())
            .header("Content-Type", MediaType.TEXT_PLAIN)
            .build();
    }

}
