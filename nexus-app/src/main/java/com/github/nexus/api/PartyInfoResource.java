package com.github.nexus.api;

import com.github.nexus.service.PartyInfoService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

@Path("/partyinfo")
public class PartyInfoResource {

    private static final Logger LOGGER = Logger.getLogger(PartyInfoResource.class.getName());

    private final PartyInfoService partyInfoService;

    public PartyInfoResource(final PartyInfoService partyInfoService) {
        this.partyInfoService = requireNonNull(partyInfoService, "transactionService must not be null");
    }

    @POST
    public Response partyInfo(final InputStream payload) throws IOException {
        return Response.status(Response.Status.CREATED).build();
    }
}
