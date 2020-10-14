package com.quorum.tessera.p2p;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.util.Base64Codec;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.*;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * <p>- creating new transactions and distributing them - deleting transactions - fetching transactions - resending old
 * transactions
 */
@Api
@Path("/")
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final TransactionManager transactionManager;

    private final BatchResendManager batchResendManager;

    private final PayloadEncoder payloadEncoder;

    private final LegacyResendManager legacyResendManager;

    public TransactionResource(final TransactionManager transactionManager,
                               final BatchResendManager batchResendManager,
                               final PayloadEncoder payloadEncoder,
                               final LegacyResendManager legacyResendManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
        this.batchResendManager = Objects.requireNonNull(batchResendManager);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.legacyResendManager = Objects.requireNonNull(legacyResendManager);
    }

    @ApiOperation("Resend transactions for given key or message hash/recipient")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded payload when TYPE is INDIVIDUAL", response = String.class),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("resend")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response resend(
            @ApiParam(name = "resendRequest", required = true) @Valid @NotNull final ResendRequest resendRequest) {

        LOGGER.debug("Received resend request");

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

        com.quorum.tessera.transaction.ResendRequest request =
                com.quorum.tessera.transaction.ResendRequest.Builder.create()
                        .withType(
                                com.quorum.tessera.transaction.ResendRequest.ResendRequestType.valueOf(
                                        resendRequest.getType().name()))
                        .withRecipient(recipient)
                        .withHash(transactionHash)
                        .build();

        com.quorum.tessera.transaction.ResendResponse response = legacyResendManager.resend(request);

        Response.ResponseBuilder builder = Response.ok();
        Optional.ofNullable(response.getPayload()).map(payloadEncoder::encode).ifPresent(builder::entity);
        return builder.build();
    }

    @ApiOperation("Resend transaction batches for given recipient key")
    @ApiResponses({
        @ApiResponse(code = 200, message = "The transaction total that has been pushed", response = String.class),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("resendBatch")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response resendBatch(
            @ApiParam(name = "resendBatchRequest", required = true) @Valid @NotNull
                    final ResendBatchRequest resendBatchRequest) {

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

    @ApiOperation(value = "Transmit encrypted payload between P2PRestApp Nodes")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Key created status"),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("push")
    @Consumes(APPLICATION_OCTET_STREAM)
    public Response push(
            @ApiParam(name = "payload", required = true, value = "Key data to be stored.") final byte[] payload) {

        LOGGER.debug("Received push request");

        final MessageHash messageHash = transactionManager.storePayload(payloadEncoder.decode(payload));
        LOGGER.debug("Push request generated hash {}", messageHash);
        // TODO: Return the query url not the string of the messageHash
        return Response.status(Response.Status.CREATED).entity(Objects.toString(messageHash)).build();
    }
}
