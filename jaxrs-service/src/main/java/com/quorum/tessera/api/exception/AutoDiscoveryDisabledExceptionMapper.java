package com.quorum.tessera.api.exception;

import com.quorum.tessera.partyinfo.AutoDiscoveryDisabledException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AutoDiscoveryDisabledExceptionMapper implements ExceptionMapper<AutoDiscoveryDisabledException> {

    @Override
    public Response toResponse(AutoDiscoveryDisabledException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }

}
