package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.DomainFilter;
import com.quorum.tessera.node.PartyInfoParser;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.node.model.PartyInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
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

    public PartyInfoResource(final PartyInfoService partyInfoService, final PartyInfoParser partyInfoParser) {
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

        return Response.status(Response.Status.OK).entity(streamingOutput).build();
    }

    @GET
    @DomainFilter
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch network/peer information", produces = "public list of peers/publickey mappings")
    @ApiResponses({@ApiResponse(code = 200, message = "Peer/Network information", response = PartyInfo.class)})
    public Response getPartyInfo() {

        final PartyInfo current = this.partyInfoService.getPartyInfo();

        //TODO: remove the filter when URIs don't need to end with a /
        final JsonArrayBuilder peersBuilder = Json.createArrayBuilder();
        current.getParties()
            .stream()
            .filter(p -> p.getUrl().endsWith("/"))
            .map(party -> {
                final JsonObjectBuilder builder = Json.createObjectBuilder();
                builder.add("url", party.getUrl());
                if (party.getLastContacted() != null) {
                    builder.add("lastContact", party.getLastContacted().toString());
                } else {
                    builder.addNull("lastContact");
                }
                return builder.build();
            })
            .forEach(peersBuilder::add);

        final JsonArrayBuilder recipientBuilder = Json.createArrayBuilder();
        current.getRecipients()
            .stream()
            .map(recipient -> Json
                .createObjectBuilder()
                .add("key", recipient.getKey().encodeToBase64())
                .add("url", recipient.getUrl())
                .build()
            ).forEach(recipientBuilder::add);

        final String output = Json
            .createObjectBuilder()
            .add("url", current.getUrl())
            .add("peers", peersBuilder.build())
            .add("keys", recipientBuilder.build())
            .build()
            .toString();

        return Response.status(Response.Status.OK).entity(output).build();
    }

}
