package com.quorum.tessera.p2p;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static java.util.Collections.emptyList;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.shared.Constants;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "peer-to-peer")
@Path("/")
public class RecoveryResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryResource.class);

  private final TransactionManager transactionManager;

  private final BatchResendManager batchResendManager;

  public RecoveryResource(
      TransactionManager transactionManager, BatchResendManager batchResendManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.batchResendManager = Objects.requireNonNull(batchResendManager);
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
            pushBatchRequest.getEncodedPayloads(), EncodedPayloadCodec.LEGACY);

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
  public Response push(
      final byte[] payload, @HeaderParam(Constants.API_VERSION_HEADER) final List<String> headers) {

    LOGGER.debug("Received push request during recovery mode");

    final Set<String> versions =
        Optional.ofNullable(headers).orElse(emptyList()).stream()
            .filter(Objects::nonNull)
            .flatMap(v -> Arrays.stream(v.split(",")))
            .collect(Collectors.toSet());

    final EncodedPayloadCodec codec = EncodedPayloadCodec.getPreferredCodec(versions);

    final PayloadEncoder payloadEncoder = PayloadEncoder.create(codec);

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
