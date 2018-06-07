package com.github.nexus.api;

import com.github.nexus.node.PartyInfo;
import com.github.nexus.node.PartyInfoParser;
import com.github.nexus.node.PartyInfoService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

@Path("/partyinfo")
public class PartyInfoResource {

    private static final Logger LOGGER = Logger.getLogger(PartyInfoResource.class.getName());

    private PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    public PartyInfoResource(final PartyInfoService partyInfoService,PartyInfoParser partyInfoParser) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser);
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response partyInfo(final byte[] payload)  {

        PartyInfo partyInfo = partyInfoParser.from(payload);

        PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

        byte[] encoded = partyInfoParser.to(updatedPartyInfo);

        return Response.status(Response.Status.CREATED)
            .entity(Entity.entity(encoded,MediaType.APPLICATION_OCTET_STREAM_TYPE))
            .build();
    }
}
