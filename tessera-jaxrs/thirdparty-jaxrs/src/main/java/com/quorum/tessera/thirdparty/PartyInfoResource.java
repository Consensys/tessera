package com.quorum.tessera.thirdparty;

import static java.util.Objects.requireNonNull;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.thirdparty.model.GetPublicKeysResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Tag(name = "third-party")
@Path("/partyinfo")
public class PartyInfoResource {

  private final Discovery discovery;

  public PartyInfoResource(final Discovery discovery) {
    this.discovery = requireNonNull(discovery, "discovery must not be null");
  }

  @Operation(
      summary = "/partyinfo/keys",
      operationId = "getPartiesPublicKeys",
      description =
          "get public keys of all known nodes in the network, including the server's own keys")
  @ApiResponse(
      responseCode = "200",
      description = "known nodes' public keys",
      content = @Content(schema = @Schema(implementation = GetPublicKeysResponse.class)))
  @GET
  @Path("/keys")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPartyInfoKeys() {

    final NodeInfo current = this.discovery.getCurrent();

    final JsonArrayBuilder recipientBuilder = Json.createArrayBuilder();
    current.getRecipients().stream()
        .map(
            recipient ->
                Json.createObjectBuilder().add("key", recipient.getKey().encodeToBase64()).build())
        .forEach(recipientBuilder::add);

    final String output =
        Json.createObjectBuilder().add("keys", recipientBuilder.build()).build().toString();

    return Response.status(Response.Status.OK).entity(output).build();
  }
}
