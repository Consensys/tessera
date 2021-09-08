package com.quorum.tessera.q2t;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import com.quorum.tessera.api.*;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.json.Json;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tag(name = "quorum-to-tessera")
@Path("/")
public class PrivacyGroupResource {

  private final PrivacyGroupManager privacyGroupManager;

  public PrivacyGroupResource(PrivacyGroupManager privacyGroupManager) {
    this.privacyGroupManager = privacyGroupManager;
  }

  @Operation(
      summary = "/createPrivacyGroup",
      operationId = "createPrivacyGroup",
      description = "creates a privacy group, stores data in database, and distribute to members",
      requestBody =
          @RequestBody(
              content =
                  @Content(
                      mediaType = APPLICATION_JSON,
                      schema = @Schema(implementation = PrivacyGroupRequest.class))))
  @ApiResponse(
      responseCode = "200",
      description = "created privacy group",
      content =
          @Content(
              mediaType = APPLICATION_JSON,
              schema = @Schema(implementation = PrivacyGroupResponse.class)))
  @ApiResponse(responseCode = "403", description = "privacy group not supported on remote member")
  @POST
  @Path("createPrivacyGroup")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createPrivacyGroup(@NotNull final PrivacyGroupRequest request) {

    final PublicKey from =
        Optional.ofNullable(request.getFrom())
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .orElseGet(privacyGroupManager::defaultPublicKey);

    final List<PublicKey> members =
        Stream.ofNullable(request.getAddresses())
            .flatMap(Arrays::stream)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .collect(Collectors.toList());

    final byte[] randomSeed =
        Optional.ofNullable(request.getSeed())
            .map(Base64.getDecoder()::decode)
            .orElseGet(generateRandomSeed);

    final String name = Optional.ofNullable(request.getName()).orElse("");
    final String description = Optional.ofNullable(request.getDescription()).orElse("");

    final PrivacyGroup created =
        privacyGroupManager.createPrivacyGroup(name, description, from, members, randomSeed);

    return Response.status(Response.Status.OK).entity(toResponseObject(created)).build();
  }

  @Operation(
      summary = "/findPrivacyGroup",
      operationId = "findPrivacyGroup",
      description = "find all the privacy groups that contain the specified members",
      requestBody =
          @RequestBody(
              content =
                  @Content(
                      mediaType = APPLICATION_JSON,
                      schema = @Schema(implementation = PrivacyGroupSearchRequest.class))))
  @ApiResponse(
      responseCode = "200",
      description =
          "An array of privacy group objects for all privacy groups containing only the specified members",
      content =
          @Content(
              mediaType = APPLICATION_JSON,
              array = @ArraySchema(schema = @Schema(implementation = PrivacyGroupResponse.class))))
  @POST
  @Path("findPrivacyGroup")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response findPrivacyGroup(@NotNull final PrivacyGroupSearchRequest searchRequest) {

    final List<PublicKey> members =
        Stream.of(searchRequest.getAddresses())
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .collect(Collectors.toList());

    final List<PrivacyGroup> privacyGroups = privacyGroupManager.findPrivacyGroup(members);

    final PrivacyGroupResponse[] results =
        privacyGroups.stream().map(this::toResponseObject).toArray(PrivacyGroupResponse[]::new);

    return Response.status(Response.Status.OK).type(APPLICATION_JSON).entity(results).build();
  }

  @Operation(
      summary = "/retrievePrivacyGroup",
      operationId = "retrievePrivacyGroup",
      description = "retrieve privacy group from a privacy group id",
      requestBody =
          @RequestBody(
              content =
                  @Content(
                      mediaType = APPLICATION_JSON,
                      schema = @Schema(implementation = PrivacyGroupRetrieveRequest.class))))
  @ApiResponse(
      responseCode = "200",
      description = "A privacy group object",
      content =
          @Content(
              mediaType = APPLICATION_JSON,
              schema = @Schema(implementation = PrivacyGroupResponse.class)))
  @ApiResponse(responseCode = "404", description = "privacy group not found")
  @POST
  @Path("retrievePrivacyGroup")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response retrievePrivacyGroup(@NotNull final PrivacyGroupRetrieveRequest retrieveRequest) {

    final PrivacyGroup.Id privacyGroupId =
        PrivacyGroup.Id.fromBase64String(retrieveRequest.getPrivacyGroupId());

    final PrivacyGroup privacyGroup = privacyGroupManager.retrievePrivacyGroup(privacyGroupId);

    return Response.ok().entity(toResponseObject(privacyGroup)).build();
  }

  @Operation(
      summary = "/deletePrivacyGroup",
      operationId = "deletePrivacyGroup",
      description = "mark a privacy group as deleted",
      requestBody =
          @RequestBody(
              content =
                  @Content(
                      mediaType = APPLICATION_JSON,
                      schema = @Schema(implementation = PrivacyGroupDeleteRequest.class))))
  @ApiResponse(
      responseCode = "200",
      description = "id of the deleted privacy group",
      content =
          @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = String.class)))
  @ApiResponse(responseCode = "404", description = "privacy group not found")
  @POST
  @Path("deletePrivacyGroup")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response deletePrivacyGroup(@NotNull final PrivacyGroupDeleteRequest request) {

    final PublicKey from =
        Optional.ofNullable(request.getFrom())
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .orElseGet(privacyGroupManager::defaultPublicKey);

    final PrivacyGroup.Id privacyGroupId =
        PrivacyGroup.Id.fromBase64String(request.getPrivacyGroupId());

    final PrivacyGroup privacyGroup = privacyGroupManager.deletePrivacyGroup(from, privacyGroupId);

    // Have to output in this format to match what is expected from Besu
    final String output =
        Json.createArrayBuilder()
            .add(privacyGroup.getId().getBase64())
            .build()
            .getJsonString(0)
            .toString();

    return Response.ok().entity(output).build();
  }

  @GET
  @Path("groups/{type}")
  @Produces(APPLICATION_JSON)
  public Response getPrivacyGroups(@PathParam("type") String type) {

    final PrivacyGroup.Type queryType = PrivacyGroup.Type.valueOf(type.toUpperCase());

    final RuntimeContext runtimeContext = RuntimeContext.getInstance();

    if (queryType == PrivacyGroup.Type.RESIDENT && !runtimeContext.isMultiplePrivateStates()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity("Multiple private state feature is not available on this privacy manager")
          .build();
    }

    final List<PrivacyGroup> privacyGroups = privacyGroupManager.findPrivacyGroupByType(queryType);

    final PrivacyGroupResponse[] results =
        privacyGroups.stream().map(this::toResponseObject).toArray(PrivacyGroupResponse[]::new);

    return Response.status(Response.Status.OK).type(APPLICATION_JSON).entity(results).build();
  }

  PrivacyGroupResponse toResponseObject(final PrivacyGroup privacyGroup) {
    return new PrivacyGroupResponse(
        privacyGroup.getId().getBase64(),
        privacyGroup.getName(),
        privacyGroup.getDescription(),
        privacyGroup.getType().name(),
        privacyGroup.getMembers().stream().map(PublicKey::encodeToBase64).toArray(String[]::new));
  }

  private final Supplier<byte[]> generateRandomSeed =
      () -> {
        final SecureRandom random = new SecureRandom();
        byte[] generated = new byte[20];
        random.nextBytes(generated);
        return generated;
      };
}
