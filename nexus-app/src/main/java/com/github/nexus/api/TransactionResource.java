package com.github.nexus.api;

import com.github.nexus.api.model.*;
import com.github.nexus.enclave.Enclave;
import com.github.nexus.util.Base64Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Path("")
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final Enclave enclave;

    private final Base64Decoder base64Decoder;

    public TransactionResource(final Enclave enclave, final Base64Decoder base64Decoder) {
        this.enclave = requireNonNull(enclave, "enclave must not be null");
        this.base64Decoder = requireNonNull(base64Decoder, "decoder must not be null");
    }

    @POST
    @Path("/send")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response send(@Valid final SendRequest sendRequest) {

        LOGGER.debug("Received send request");

        final String sender = sendRequest.getFrom();
        final Optional<byte[]> from = Optional.ofNullable(sender)
            .map(base64Decoder::decode);

        LOGGER.debug("SEND: sender {}", sender);

        final byte[][] recipients = Stream
            .of(sendRequest.getTo())
            .map(base64Decoder::decode)
            .toArray(byte[][]::new);

        LOGGER.debug("SEND: recipients {}", Stream.of(sendRequest.getTo()).collect(Collectors.joining()));

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);
        final SendResponse response = new SendResponse(encodedKey);

        return Response.status(Response.Status.OK)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .entity(response)
            .build();

    }

    @POST
    @Path("/sendraw")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendRaw(@Context final HttpHeaders headers, final byte[] payload) {

        final String sender = headers.getHeaderString("c11n-from");
        final Optional<byte[]> from = Optional.ofNullable(sender)
            .map(base64Decoder::decode);

        final byte[][] recipients = headers.getRequestHeader("c11n-to")
            .stream()
            .map(base64Decoder::decode)
            .toArray(byte[][]::new);

        final byte[] key = enclave.store(from, recipients, payload).getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);

        //TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Response.Status.OK)
            .entity(encodedKey)
            .build();
    }

    @GET
    @Path("/receive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receive(@Valid final ReceiveRequest receiveRequest) {

        final byte[] key = base64Decoder.decode(receiveRequest.getKey());

        final Optional<byte[]> to = Optional
            .ofNullable(receiveRequest.getTo())
            .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(key, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        final ReceiveResponse response = new ReceiveResponse(encodedPayload);

        return Response.status(Response.Status.OK)
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .entity(response)
            .build();

    }

    @GET
    @Path("/receiveraw")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public Response receiveRaw(@Context final HttpHeaders headers) {

        final byte[] key = base64Decoder.decode(headers.getHeaderString("c11n-key"));

        final Optional<byte[]> to = Optional
            .ofNullable(headers.getHeaderString("c11n-to"))
            .map(base64Decoder::decode);

        final byte[] payload = enclave.receive(key, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        return Response.status(Response.Status.OK)
            .entity(encodedPayload)
            .build();
    }

    @POST
    @Path("/delete")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@Valid final DeleteRequest deleteRequest) {

        final byte[] hashBytes = base64Decoder.decode(deleteRequest.getKey());

        enclave.delete(hashBytes);

        return Response.status(Response.Status.OK)
            .entity("Delete successful")
            .build();

    }

    @POST
    @Path("/resend")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response resend(@Valid final ResendRequest resendRequest) {

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

    @POST
    @Path("/push")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response push(final byte[] payload) {
        LOGGER.info(Base64.getEncoder().encodeToString(enclave.storePayload(payload).getHashBytes()));

        return Response.status(Response.Status.CREATED).build();
    }


}
