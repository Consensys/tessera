package com.github.nexus.api;

import com.github.nexus.node.PartyInfoParser;
import com.github.nexus.node.PartyInfoService;
import com.github.nexus.node.model.PartyInfo;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

@Path("/partyinfo")
public class PartyInfoResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoResource.class);

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    public PartyInfoResource(final PartyInfoService partyInfoService,
                             final PartyInfoParser partyInfoParser) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser);
    }


    @ApiResponses({
        @ApiResponse(code = 200,message = "Endcoded PartyInfo Data",response = byte[].class)
    })
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response partyInfo(@ApiParam(required = true) final byte[] payload) {

        final PartyInfo partyInfo = partyInfoParser.from(payload);

        final PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

        byte[] encoded = partyInfoParser.to(updatedPartyInfo);

        StreamingOutput streamingOutput = (OutputStream out) -> {
            out.write(encoded);
        };

        return Response.status(Response.Status.OK)
            .entity(streamingOutput)
            .build();
    }
}
