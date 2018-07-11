package com.github.tessera.api;

import com.github.tessera.node.PartyInfoParser;
import com.github.tessera.node.PartyInfoService;
import com.github.tessera.node.model.PartyInfo;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiOperation;

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

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    public PartyInfoResource(final PartyInfoService partyInfoService,
                             final PartyInfoParser partyInfoParser) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
    }

    @ApiOperation(value = "Request public key/url of other nodes", produces = "public keylist/url")
    @ApiResponses({
        @ApiResponse(code = 200,message = "Encoded PartyInfo Data",response = byte[].class)
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
