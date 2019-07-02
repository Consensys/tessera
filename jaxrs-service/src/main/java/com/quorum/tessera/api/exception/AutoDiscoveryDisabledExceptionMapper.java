package com.quorum.tessera.api.exception;

import com.quorum.tessera.partyinfo.AutoDiscoveryDisabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AutoDiscoveryDisabledExceptionMapper implements ExceptionMapper<AutoDiscoveryDisabledException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDiscoveryDisabledExceptionMapper.class);

    @Override
    public Response toResponse(final AutoDiscoveryDisabledException exception) {
        LOGGER.debug(null, exception);

        return Response.status(Response.Status.FORBIDDEN)
            .entity(exception.getMessage())
            .type(MediaType.TEXT_PLAIN)
            .build();
    }

}
