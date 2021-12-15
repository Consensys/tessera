package com.quorum.tessera.api.common;

import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import com.quorum.tessera.api.StoreRawRequest;
import com.quorum.tessera.api.StoreRawResponse;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

/** Provides endpoints for dealing with raw transactions */
@Tags({@Tag(name = "quorum-to-tessera"), @Tag(name = "third-party")})
@Path("/")
public class RawTransactionResource {

  public static final String ENDPOINT_STORE_RAW = "storeraw";

  private final TransactionManager transactionManager;

  public RawTransactionResource() {
    this(TransactionManager.create());
  }

  public RawTransactionResource(final TransactionManager transactionManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
  }

  // hide this operation from swagger generation; the /storeraw operation is overloaded and must be
  // documented in a single place
  @Hidden
  @POST
  @Path(ENDPOINT_STORE_RAW)
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response store(
      @RequestBody(
              required = true,
              content = @Content(schema = @Schema(implementation = StoreRawRequest.class)))
          @NotNull
          @Valid
          final StoreRawRequest request) {
    final StoreRawResponse storeRawResponse = this.forwardRequest(request);
    return Response.ok().type(APPLICATION_JSON).entity(storeRawResponse).build();
  }

  // path /storeraw is overloaded (application/json and application/vnd.tessera-2.1+json); swagger
  // annotations cannot handle situations like this so this operation documents both
  @Operation(
      summary = "/storeraw",
      operationId = "encryptAndStoreVersion",
      description = "encrypts a payload and stores result in the \"raw\" database",
      requestBody =
          @RequestBody(
              required = true,
              content = {
                @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = StoreRawRequest.class)),
                @Content(
                    mediaType = MIME_TYPE_JSON_2_1,
                    schema = @Schema(implementation = StoreRawRequest.class))
              }))
  @ApiResponse(
      responseCode = "200",
      description = "hash of encrypted payload",
      content = {
        @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = StoreRawResponse.class)),
        @Content(
            mediaType = MIME_TYPE_JSON_2_1,
            schema = @Schema(implementation = StoreRawResponse.class))
      })
  @ApiResponse(responseCode = "404", description = "'from' key in request body not found")
  @POST
  @Path(ENDPOINT_STORE_RAW)
  @Consumes(MIME_TYPE_JSON_2_1)
  @Produces(MIME_TYPE_JSON_2_1)
  public Response storeVersion21(@NotNull @Valid final StoreRawRequest request) {
    final StoreRawResponse storeRawResponse = this.forwardRequest(request);
    return Response.ok().type(MIME_TYPE_JSON_2_1).entity(storeRawResponse).build();
  }

  private StoreRawResponse forwardRequest(final StoreRawRequest request) {
    final PublicKey sender =
        request.getFrom().map(PublicKey::from).orElseGet(transactionManager::defaultPublicKey);

    final com.quorum.tessera.transaction.StoreRawRequest storeRawRequest =
        com.quorum.tessera.transaction.StoreRawRequest.Builder.create()
            .withSender(sender)
            .withPayload(request.getPayload())
            .build();

    final com.quorum.tessera.transaction.StoreRawResponse response =
        transactionManager.store(storeRawRequest);

    final StoreRawResponse storeRawResponse = new StoreRawResponse();
    storeRawResponse.setKey(response.getHash().getHashBytes());

    return storeRawResponse;
  }
}
