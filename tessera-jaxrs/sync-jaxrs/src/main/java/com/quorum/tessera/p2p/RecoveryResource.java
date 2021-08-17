package com.quorum.tessera.p2p;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "peer-to-peer")
@Path("/")
public class RecoveryResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryResource.class);

  private final TransactionManager transactionManager;

  private final BatchResendManager batchResendManager;

  private final PayloadEncoder payloadEncoder;

  public RecoveryResource(
      TransactionManager transactionManager,
      BatchResendManager batchResendManager,
      PayloadEncoder payloadEncoder) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.batchResendManager = Objects.requireNonNull(batchResendManager);
    this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
  }

  @Operation(
      summary = "/pushBatch",
      operationId = "pushPayloadBatch",
      description =
          "store batch of encoded payloads to the server's database (available only when the server is in recovery mode)")
  @ApiResponse(responseCode = "200", description = "batch successfully stored")
  @POST
  @Path("pushBatch")
  @Consumes(APPLICATION_JSON)
  public Response pushBatch(@Valid @NotNull final PushBatchRequest pushBatchRequest) {

    LOGGER.debug("Received push request");

    com.quorum.tessera.recovery.resend.PushBatchRequest request =
        com.quorum.tessera.recovery.resend.PushBatchRequest.from(
            pushBatchRequest.getEncodedPayloads());

    batchResendManager.storeResendBatch(request);

    LOGGER.debug("Push batch processed successfully");
    return Response.status(Response.Status.OK).build();
  }

  // path /push with application/octet-stream is overloaded (RecoveryResource &
  // TransactionResource); swagger annotations cannot handle situations like this so hide this
  // operation and use TransactionResource::push to document both
  @Hidden
  @POST
  @Path("push")
  @Consumes(APPLICATION_OCTET_STREAM)
  public Response push(final byte[] payload) {

    LOGGER.debug("Received push request during recovery mode");

    final EncodedPayload encodedPayload = payloadEncoder.decode(payload);

    if (encodedPayload.getPrivacyMode() != PrivacyMode.STANDARD_PRIVATE) {
      return Response.status(Response.Status.FORBIDDEN)
          .entity("Transactions with enhanced privacy are not accepted during recovery mode")
          .build();
    }

    final MessageHash messageHash = transactionManager.storePayload(encodedPayload);
    LOGGER.debug("Push request generated hash {}", messageHash);

    return Response.status(Response.Status.CREATED).entity(Objects.toString(messageHash)).build();
  }
}
