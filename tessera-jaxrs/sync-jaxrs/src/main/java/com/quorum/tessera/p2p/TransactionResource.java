package com.quorum.tessera.p2p;

import com.quorum.tessera.base64.Base64Codec;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionManager;
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
import javax.ws.rs.core.Response.Status;
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

    private final PayloadEncoder encoder;

    public TransactionResource(final TransactionManager delegate, final PayloadEncoder payloadEncoder) {
        this.transactionManager = Objects.requireNonNull(delegate);
        this.encoder = Objects.requireNonNull(payloadEncoder);
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
                Optional.ofNullable(resendRequest)
                        .map(ResendRequest::getKey)
                        .map(Base64.getDecoder()::decode)
                        .map(MessageHash::new)
                        .orElse(null);

        com.quorum.tessera.transaction.ResendRequest request =
                com.quorum.tessera.transaction.ResendRequest.Builder.create()
                        .withType(com.quorum.tessera.transaction.ResendRequest.ResendRequestType.valueOf(resendRequest.getType()))
                        .withRecipient(recipient)
                        .withHash(transactionHash)
                        .build();

        com.quorum.tessera.transaction.ResendResponse response = transactionManager.resend(request);
        Response.ResponseBuilder builder = Response.status(Status.OK);
        Optional.ofNullable(response.getPayload()).map(encoder::encode).ifPresent(builder::entity);
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

        final MessageHash messageHash = transactionManager.storePayload(encoder.decode(payload));
        LOGGER.debug("Push request generated hash {}", messageHash);
        // TODO: Return the query url not the string of the messageHash
        return Response.status(Response.Status.CREATED).entity(Objects.toString(messageHash)).build();
    }
}
