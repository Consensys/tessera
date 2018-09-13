package com.quorum.tessera.api;

import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static java.util.Objects.requireNonNull;

/**
 * Defines endpoints for requesting node discovery (partyinfo) information
 */
@Path("/partyinfo")
public class PartyInfoResource {

    private final PartyInfoParser partyInfoParser;

    private final PartyInfoService partyInfoService;

    public PartyInfoResource(final PartyInfoService partyInfoService,
                             final PartyInfoParser partyInfoParser) {
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService must not be null");
        this.partyInfoParser = requireNonNull(partyInfoParser, "partyInfoParser must not be null");
    }

    /**
     * Allows node information to be retrieved in a specific encoded form
     * including other node URLS and public key to URL mappings
     *
     * @param payload The encoded node information from the requester
     * @return the merged node information from this node, which may contain new information
     */
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Request public key/url of other nodes", produces = "public keylist/url")
    @ApiResponses({@ApiResponse(code = 200, message = "Encoded PartyInfo Data", response = byte[].class)})
    public Response partyInfo(@ApiParam(required = true) final byte[] payload) {

        final PartyInfo partyInfo = partyInfoParser.from(payload);

        final PartyInfo updatedPartyInfo = partyInfoService.updatePartyInfo(partyInfo);

        final byte[] encoded = partyInfoParser.to(updatedPartyInfo);

        final StreamingOutput streamingOutput = out -> out.write(encoded);

        return Response.status(Response.Status.OK)
            .entity(streamingOutput)
            .build();
    }
}
