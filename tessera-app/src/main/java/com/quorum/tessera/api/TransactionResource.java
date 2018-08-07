package com.quorum.tessera.api;

import com.quorum.tessera.api.filter.PrivateApi;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.transaction.PayloadEncoderImpl;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.util.Base64Decoder;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.*;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * - creating new transactions and distributing them
 * - deleting transactions
 * - fetching transactions
 * - resending old transactions
 */
@Path("/")
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final Enclave enclave;

    private final Base64Decoder base64Decoder;

    public TransactionResource(final Enclave enclave, final Base64Decoder base64Decoder) {
        this.enclave = requireNonNull(enclave, "enclave must not be null");
        this.base64Decoder = requireNonNull(base64Decoder, "decoder must not be null");
    }

    @ApiOperation(value = "Send private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @PrivateApi
    @Path("send")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response send(
            @ApiParam(name = "sendRequest", required = true)
            @Valid final SendRequest sendRequest) {

        LOGGER.debug("Received send request");

        final String sender = sendRequest.getFrom();
        final Optional<byte[]> from = Optional.ofNullable(sender)
                .map(base64Decoder::decode);

        LOGGER.debug("SEND: sender {}", sender);

        final byte[][] recipients = Stream
                .of(sendRequest.getTo())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        LOGGER.debug("SEND: recipients {}", Stream.of(sendRequest.getTo()).collect(joining()));

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);
        final SendResponse response = new SendResponse(encodedKey);

        return Response.status(Response.Status.OK)
                .header("Content-Type", APPLICATION_JSON)
                .entity(response)
                .build();

    }

    @ApiOperation(value = "Send private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded Key", response = String.class),
        @ApiResponse(code = 500, message = "Unknown server error")
    })
    @POST
    @PrivateApi
    @Path("sendraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(TEXT_PLAIN)
    public Response sendRaw(@HeaderParam("c11n-from") final String sender,
            @HeaderParam("c11n-to") final String recipientKeys,
            @NotNull @Size(min = 1) final byte[] payload) {

        final Optional<byte[]> from = Optional
                .ofNullable(sender)
                .map(base64Decoder::decode);

        final String nonnullRecipients = Optional.ofNullable(recipientKeys).orElse("");
        final byte[][] recipients = Stream.of(nonnullRecipients.split(","))
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        LOGGER.debug("SendRaw Recipients: {}", nonnullRecipients);

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);

        LOGGER.debug("Encoded key: {}", encodedKey);

        //TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Response.Status.OK)
                .entity(encodedKey)
                .build();
    }
    @ApiOperation(value = "Returns decrypted payload back to Quorum")
    @ApiResponses({
        @ApiResponse(code = 200, response = ReceiveResponse.class, message = "Receive Response object")
    })
    @GET
    @PrivateApi
    @Path("/transaction/{hash}")
    @Produces(APPLICATION_JSON)
    public Response receive(
            @ApiParam("Encoded hash used to decrypt the payload")
            @NotNull @Valid @PathParam("hash") final String hash,
            @ApiParam("Encoded recipient key")
            @Valid @QueryParam("to") final String toStr
    ) {

        final byte[] key = base64Decoder.decode(hash);

        final Optional<byte[]> to = Optional
                .ofNullable(toStr)
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(key, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        final ReceiveResponse response = new ReceiveResponse(encodedPayload);

        return Response.status(Response.Status.OK)
                .header("Content-Type", APPLICATION_JSON)
                .entity(response)
                .build();

    }

    @GET
    @Deprecated
    @PrivateApi
    @Path("/receive")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response receive(@Valid final ReceiveRequest request) {

        LOGGER.debug("Received receive request");

        return this.receive(request.getKey(), request.getTo());
    }

    @ApiOperation(value = "Submit keys to retrieve payload and decrypt it", produces = "Unencrypted payload")
    @ApiResponses({@ApiResponse(code = 200, message = "Raw payload", response = byte[].class)})
    @GET
    @Path("receiveraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response receiveRaw(
            @ApiParam("Encoded transaction hash")
            @NotNull @HeaderParam(value = "c11n-key") String hash,
            @ApiParam("Encoded Recipient Public Key")
            @HeaderParam(value = "c11n-to") String recipientKey) {

        LOGGER.debug("Received receiveraw request");

        final byte[] decodedKey = base64Decoder.decode(hash);

        final Optional<byte[]> to = Optional
                .ofNullable(recipientKey)
                .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(decodedKey, to);

        return Response.status(Response.Status.OK)
            .entity(payload)
            .build();
    }

    @Deprecated
    @ApiOperation("Deprecated: Replaced by /transaction/{key} DELETE HTTP method")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Status message", response = String.class),
        @ApiResponse(code = 404, message = "If the entity doesn't exist")
    })
    @POST
    @Path("delete")
    @Consumes(APPLICATION_JSON)
    @Produces(TEXT_PLAIN)
    public Response delete(
            @ApiParam(name = "deleteRequest", required = true, value = "Key data to be deleted")
            @Valid final DeleteRequest deleteRequest) {

        LOGGER.debug("Received deprecated delete request");

        this.deleteKey(deleteRequest.getKey());

        return Response.status(Response.Status.OK)
                .entity("Delete successful")
                .build();

    }
    @ApiOperation("Delete single transaction from Tessera node")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Successful deletion"),
        @ApiResponse(code = 404, message = "If the entity doesn't exist")
    })
    @DELETE
    @Path("/transaction/{key}")
    public Response deleteKey(@ApiParam("Encoded hash") @PathParam("key") final String key) {

        LOGGER.debug("Received delete key request");

        final byte[] hashBytes = base64Decoder.decode(key);
        enclave.delete(hashBytes);

        return Response.noContent().build();
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
        @ApiParam(name = "resendRequest", required = true) @Valid @NotNull final ResendRequest resendRequest
    ) {

        LOGGER.debug("Received resend request");

        final byte[] publicKey = base64Decoder.decode(resendRequest.getPublicKey());

        if (resendRequest.getType() == ResendRequestType.ALL) {
            enclave.resendAll(publicKey);
        } else if (resendRequest.getType() == ResendRequestType.INDIVIDUAL) {

            final byte[] hashKey = base64Decoder.decode(resendRequest.getKey());

            final EncodedPayloadWithRecipients payloadWithRecipients = enclave
                .fetchTransactionForRecipient(new MessageHash(hashKey), new Key(publicKey));

            final byte[] encoded = new PayloadEncoderImpl().encode(payloadWithRecipients);

            return Response.status(Response.Status.OK)
                    .entity(encoded)
                    .build();
        }

        return Response.status(Response.Status.OK).build();
    }
    @ApiOperation(value = "Transmit encrypted payload between Tessera Nodes")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Key created status"),
        @ApiResponse(code = 500, message = "General error")
    })
    @POST
    @Path("push")
    @Consumes(APPLICATION_OCTET_STREAM)
    public Response push(
        @ApiParam(name = "payload", required = true, value = "Key data to be stored.") final byte[] payload
    ) {

        LOGGER.debug("Received push request");

        final MessageHash messageHash = enclave.storePayload(payload);

        LOGGER.info(base64Decoder.encodeToString(messageHash.getHashBytes()));

        return Response.status(Response.Status.CREATED).build();
    }

}
