package com.quorum.tessera.p2p;

import static jakarta.ws.rs.core.MediaType.*;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * <p>- creating new transactions and distributing them - deleting transactions - fetching
 * transactions - resending old transactions
 */
@Tag(name = "peer-to-peer")
@Path("/")
public class TransactionResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

  private final TransactionManager transactionManager;

  private final BatchResendManager batchResendManager;

  private final PayloadEncoder payloadEncoder;

  private final LegacyResendManager legacyResendManager;

  public TransactionResource(
      final TransactionManager transactionManager,
      final BatchResendManager batchResendManager,
      final PayloadEncoder payloadEncoder,
      final LegacyResendManager legacyResendManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.batchResendManager = Objects.requireNonNull(batchResendManager);
    this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
    this.legacyResendManager = Objects.requireNonNull(legacyResendManager);
  }

  @Operation(
      summary = "/resend",
      operationId = "requestPayloadResend",
      description =
          "initiate resend of either an INDIVIDUAL transaction or ALL transactions involving a given public key")
  @ApiResponse(
      responseCode = "200",
      description = "resent payload",
      content =
          @Content(
              array =
                  @ArraySchema(
                      schema =
                          @Schema(
                              description =
                                  "empty if request was for ALL; else the encoded INDIVIDUAL transaction",
                              type = "string",
                              format = "byte"))))
  @POST
  @Path("resend")
  @Consumes(APPLICATION_JSON)
  @Produces(TEXT_PLAIN)
  public Response resend(@Valid @NotNull final ResendRequest resendRequest) {

    LOGGER.debug("Received resend request {}", resendRequest);

    PublicKey recipient =
        Optional.of(resendRequest)
            .map(ResendRequest::getPublicKey)
            .map(Base64Codec.create()::decode)
            .map(PublicKey::from)
            .get();

    MessageHash transactionHash =
        Optional.of(resendRequest)
            .map(ResendRequest::getKey)
            .map(Base64.getDecoder()::decode)
            .map(MessageHash::new)
            .orElse(null);

    com.quorum.tessera.recovery.resend.ResendRequest request =
        com.quorum.tessera.recovery.resend.ResendRequest.Builder.create()
            .withType(
                com.quorum.tessera.recovery.resend.ResendRequest.ResendRequestType.valueOf(
                    resendRequest.getType()))
            .withRecipient(recipient)
            .withHash(transactionHash)
            .build();

    com.quorum.tessera.recovery.resend.ResendResponse response =
        legacyResendManager.resend(request);

    Response.ResponseBuilder builder = Response.ok();
    Optional.ofNullable(response.getPayload())
        .map(payloadEncoder::encode)
        .ifPresent(builder::entity);
    return builder.build();
  }

  @Operation(
      summary = "/resendBatch",
      operationId = "requestPayloadBatchResend",
      description = "initiate resend of all transactions for a given public key in batches")
  @ApiResponse(
      responseCode = "200",
      description = "count of total transactions being resent",
      content =
          @Content(
              schema =
                  @Schema(
                      implementation = com.quorum.tessera.p2p.recovery.ResendBatchResponse.class)))
  @POST
  @Path("resendBatch")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response resendBatch(@Valid @NotNull final ResendBatchRequest resendBatchRequest) {

    LOGGER.debug("Received resend request");

    com.quorum.tessera.recovery.resend.ResendBatchRequest request =
        com.quorum.tessera.recovery.resend.ResendBatchRequest.Builder.create()
            .withPublicKey(resendBatchRequest.getPublicKey())
            .withBatchSize(resendBatchRequest.getBatchSize())
            .build();

    ResendBatchResponse response = batchResendManager.resendBatch(request);

    com.quorum.tessera.p2p.recovery.ResendBatchResponse responseEntity =
        new com.quorum.tessera.p2p.recovery.ResendBatchResponse();
    responseEntity.setTotal(response.getTotal());

    Response.ResponseBuilder builder = Response.status(Response.Status.OK);
    builder.entity(responseEntity);
    return builder.build();
  }

  // path push is overloaded (RecoveryResource & TransactionResource); swagger cannot handle
  // situations like this so this operation documents both
  @Operation(
      summary = "/push",
      operationId = "pushPayload",
      description = "store encoded payload to the server's database")
  @ApiResponse(
      responseCode = "201",
      description = "hash of encoded payload",
      content =
          @Content(
              mediaType = TEXT_PLAIN,
              schema =
                  @Schema(
                      description = "hash of encrypted payload",
                      type = "string",
                      format = "base64")))
  @ApiResponse(
      responseCode = "403",
      description =
          "server is in recovery mode and encoded payload is not a Standard Private transaction")
  @POST
  @Path("push")
  @Consumes(APPLICATION_OCTET_STREAM)
  public Response push(@Schema(description = "encoded payload") final byte[] payload) {

    LOGGER.debug("Received push request");
    final EncodedPayload encodedPayload = payloadEncoder.decode(payload);
    LOGGER.debug("Decoded payload {}", encodedPayload.getEncodedPayloadCodec());
    final MessageHash messageHash = transactionManager.storePayload(encodedPayload);
    LOGGER.debug("Push request generated hash {}", messageHash);
    // TODO: Return the query url not the string of the messageHash
    return Response.status(Response.Status.CREATED).entity(Objects.toString(messageHash)).build();
  }
}
