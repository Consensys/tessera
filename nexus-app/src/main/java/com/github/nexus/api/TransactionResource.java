package com.github.nexus.api;

import com.github.nexus.api.model.*;
import com.github.nexus.enclave.Enclave;
import com.github.nexus.util.Base64Decoder;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

@Path("/")
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final Enclave enclave;

    private final Base64Decoder base64Decoder;

    public TransactionResource(final Enclave enclave, final Base64Decoder base64Decoder) {
        this.enclave = requireNonNull(enclave, "enclave must not be null");
        this.base64Decoder = requireNonNull(base64Decoder, "decoder must not be null");
    }

    @ApiResponses({
        @ApiResponse(code = 200,
            response = SendResponse.class,
            message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @Path("send")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
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
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .entity(response)
            .build();

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded Key", response = String.class)
    })
    @POST
    @Path("sendraw")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
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

    @ApiResponses({
        @ApiResponse(code = 200, response = ReceiveResponse.class, message = "Receive Response object")
    })
    @GET
    @Path("/transaction/{hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response receive(
        @ApiParam(name = "hash", required = true)
        @Valid @PathParam("hash") final String hash,

        @ApiParam(name = "to")
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
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .entity(response)
            .build();

    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded value", response = String.class)
    })
    @GET
    @Path("receiveraw")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveRaw(
        @NotNull @HeaderParam(value = "c11n-key") String key,
        @HeaderParam(value = "c11n-to") String recipientKey) {

        final byte[] decodedKey = base64Decoder.decode(key);

        final Optional<byte[]> to = Optional
            .ofNullable(recipientKey)
            .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(decodedKey, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        return Response.status(Response.Status.OK)
            .entity(encodedPayload)
            .build();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Status message", response = String.class)
    })
    @POST
    @Path("delete")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(
        @ApiParam(name = "deleteRequest", required = true)
        @Valid final DeleteRequest deleteRequest) {

        final byte[] hashBytes = base64Decoder.decode(deleteRequest.getKey());

        enclave.delete(hashBytes);

        return Response.status(Response.Status.OK)
            .entity("Delete successful")
            .build();

    }

    @ApiResponses(
        {@ApiResponse(code = 200,
            message = "Encoded payload when ResendRequestType is INDIVIDUAL",
            response = String.class)
        }
    )
    @POST
    @Path("resend")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public Response resend(
        @ApiParam(name = "resendRequest", required = true)
        @Valid @NotNull final ResendRequest resendRequest) {

        final byte[] publicKey = base64Decoder.decode(resendRequest.getPublicKey());

        if (resendRequest.getType() == ResendRequestType.ALL) {
            enclave.resendAll(publicKey);
        } else if (resendRequest.getType() == ResendRequestType.INDIVIDUAL) {
            final byte[] hashKey = Base64.getDecoder().decode(resendRequest.getKey());
            final byte[] payload = enclave.receive(hashKey, Optional.of(publicKey));
            final String encodedPayload = base64Decoder.encodeToString(payload);
            return Response.status(Response.Status.OK)
                .entity(encodedPayload)
                .build();
        }

        return Response.status(Response.Status.OK).build();
    }

    @ApiResponses(
        {@ApiResponse(code = 201, message = "Key created status")}
    )
    @POST
    @Path("push")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response push(
        @ApiParam(name = "payload", required = true, value = "Key data to be stored.") final byte[] payload) {
        LOGGER.info(Base64.getEncoder().encodeToString(enclave.storePayload(payload).getHashBytes()));

        return Response.status(Response.Status.CREATED).build();
    }


}
