package com.quorum.tessera.thirdparty;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Api
@Path("/partyinfo")
public class PartyInfoResource {

    private final Discovery discovery;

    public PartyInfoResource(final Discovery discovery) {
        this.discovery = requireNonNull(discovery, "discovery must not be null");
    }

    @GET
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch network/peer public keys")
    @ApiResponses({@ApiResponse(code = 200, message = "Peer/Network public keys")})
    public Response getPartyInfoKeys() {

        final NodeInfo current = this.discovery.getCurrent();
        PartyInfo partyInfo = PartyInfo.from(current);
        final JsonArrayBuilder recipientBuilder = Json.createArrayBuilder();
        partyInfo.getRecipients().stream()
                .map(
                        recipient ->
                                Json.createObjectBuilder()
                                        .add("key", recipient.getKey().encodeToBase64())
                                        .build())
                .forEach(recipientBuilder::add);

        final String output =
                Json.createObjectBuilder()
                        .add("keys", recipientBuilder.build())
                        .build()
                        .toString();

        return Response.status(Response.Status.OK).entity(output).build();
    }
}
