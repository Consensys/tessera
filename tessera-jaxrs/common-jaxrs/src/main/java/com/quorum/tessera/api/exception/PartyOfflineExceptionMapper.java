package com.quorum.tessera.api.exception;

import com.quorum.tessera.partyinfo.PartyOfflineException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class PartyOfflineExceptionMapper implements ExceptionMapper<PartyOfflineException> {
    @Override
    public Response toResponse(PartyOfflineException exception) {
        return Response.status(503,exception.getMessage()).build();
    }
}
