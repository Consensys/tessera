package com.quorum.tessera.thirdparty;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.thirdparty.model.GetPublicKeysResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Set;

@Tag(name = "third-party")
@Path("/keys")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KeyResource {

  @GET
  @Operation(summary = "/keys", description = "get all public keys managed by the server's enclave")
  @ApiResponse(
      responseCode = "200",
      description = "server's public keys",
      content = @Content(schema = @Schema(implementation = GetPublicKeysResponse.class)))
  public Response getPublicKeys() {

    RuntimeContext runtimeContext = RuntimeContext.getInstance();

    Set<PublicKey> publicKeys = runtimeContext.getPublicKeys();

    final JsonArrayBuilder keyBuilder = Json.createArrayBuilder();

    publicKeys.stream()
        .map(key -> Json.createObjectBuilder().add("key", key.encodeToBase64()).build())
        .forEach(keyBuilder::add);

    final String output =
        Json.createObjectBuilder().add("keys", keyBuilder.build()).build().toString();

    return Response.status(Response.Status.OK).entity(output).build();
  }
}
