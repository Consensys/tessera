package com.quorum.tessera.q2t;

import com.quorum.tessera.api.filter.DomainFilter;
import com.quorum.tessera.api.filter.Logged;
import com.quorum.tessera.api.model.*;
import com.quorum.tessera.transaction.TransactionManager;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.*;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * - creating new transactions and distributing them - deleting transactions -
 * fetching transactions - resending old transactions
 */
@DomainFilter
@Logged
@Path("/")
public class TransactionResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final TransactionManager delegate;

    public TransactionResource(TransactionManager delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @ApiOperation(value = "Send private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @Path("send")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response send(
            @ApiParam(name = "sendRequest", required = true)
            @NotNull @Valid final SendRequest sendRequest) throws UnsupportedEncodingException {

        final SendResponse response = delegate.send(sendRequest);

        URI location = UriBuilder.fromPath("transaction")
                .path(URLEncoder.encode(response.getKey(), StandardCharsets.UTF_8.toString()))
                .build();

        return Response.status(Status.CREATED)
                .type(APPLICATION_JSON)
                .location(location)
                .entity(response)
                .build();

    }

    @ApiOperation(value = "Send private raw transaction payload", produces = "Encrypted payload hash")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @Path("sendsignedtx")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(TEXT_PLAIN)
    public Response sendSignedTransaction(
        @HeaderParam("c11n-to") final String recipientKeys,
        @NotNull @Size(min = 1) final byte[] signedTransaction) throws UnsupportedEncodingException {

        SendSignedRequest sendSignedRequest = new SendSignedRequest();

        sendSignedRequest.setHash(signedTransaction);

        Optional.ofNullable(recipientKeys)
            .filter(s -> !Objects.equals("", s))
            .map(v -> v.split(","))
            .ifPresent(sendSignedRequest::setTo);

        final SendResponse response = delegate.sendSignedTransaction(sendSignedRequest);

        final String encodedKey = response.getKey();

        LOGGER.debug("Encoded key: {}", encodedKey);

        URI location = UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(encodedKey, StandardCharsets.UTF_8.toString()))
            .build();

        //TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Status.OK)
            .entity(encodedKey)
            .location(location)
            .build();
    }

    @ApiOperation(value = "Send private transaction payload", produces = "Encrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Encoded Key", response = String.class),
        @ApiResponse(code = 500, message = "Unknown server error")
    })
    @POST
    @Path("sendraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(TEXT_PLAIN)
    public Response sendRaw(
            @HeaderParam("c11n-from") final String sender,
            @HeaderParam("c11n-to") final String recipientKeys,
            @NotNull @Size(min = 1) final byte[] payload) throws UnsupportedEncodingException {

        SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(sender);
        sendRequest.setPayload(payload);

        Optional.ofNullable(recipientKeys)
                .filter(s -> !Objects.equals("", s))
                .map(v -> v.split(","))
                .ifPresent(sendRequest::setTo);

        final SendResponse sendResponse = delegate.send(sendRequest);

        final String encodedKey = sendResponse.getKey();

        LOGGER.debug("Encoded key: {}", encodedKey);

        URI location = UriBuilder.fromPath("transaction")
                .path(URLEncoder.encode(encodedKey, StandardCharsets.UTF_8.toString()))
                .build();

        //TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Status.OK)
                .entity(encodedKey)
                .location(location)
                .build();
    }

    @ApiOperation(value = "Returns decrypted payload back to Quorum")
    @ApiResponses({
        @ApiResponse(code = 200, response = ReceiveResponse.class, message = "Receive Response object")
    })
    @GET
    @Path("/transaction/{hash}")
    @Produces(APPLICATION_JSON)
    public Response receive(
            @ApiParam("Encoded hash used to decrypt the payload")
            @NotNull @Valid @PathParam("hash") final String hash,
            @ApiParam("Encoded recipient key")
            @Valid @QueryParam("to") final String toStr
    ) {

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(hash);
        receiveRequest.setTo(toStr);
        ReceiveResponse response = delegate.receive(receiveRequest);

        return Response.status(Status.OK)
                .type(APPLICATION_JSON)
                .entity(response)
                .build();
    }

    @GET
    @Path("/receive")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response receive(@Valid final ReceiveRequest request) {

        LOGGER.debug("Received receive request");

        ReceiveResponse response = delegate.receive(request);

        return Response.status(Status.OK)
                .type(APPLICATION_JSON)
                .entity(response)
                .build();
    }

    @ApiOperation(value = "Submit keys to retrieve payload and decrypt it", produces = "Unencrypted payload")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Raw payload", response = byte[].class)})
    @GET
    @Path("receiveraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response receiveRaw(
            @ApiParam("Encoded transaction hash")
            @NotNull @HeaderParam(value = "c11n-key") String hash,
            @ApiParam("Encoded Recipient Public Key")
            @HeaderParam(value = "c11n-to") String recipientKey) {

        LOGGER.debug("Received receiveraw request for hash : {}, recipientKey: {}",hash,recipientKey);

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(hash);
        receiveRequest.setTo(recipientKey);

        ReceiveResponse receiveResponse = delegate.receive(receiveRequest);

        byte[] payload = receiveResponse.getPayload();

        return Response.status(Status.OK)
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

        delegate.delete(deleteRequest);

        return Response.status(Response.Status.OK)
            .entity("Delete successful")
            .build();
    }

    @ApiOperation("Delete single transaction from P2PRestApp node")
    @ApiResponses({
        @ApiResponse(code = 204, message = "Successful deletion"),
        @ApiResponse(code = 404, message = "If the entity doesn't exist")
    })
    @DELETE
    @Path("/transaction/{key}")
    public Response deleteKey(@ApiParam("Encoded hash") @PathParam("key") final String key) {

        LOGGER.debug("Received delete key request");

        DeleteRequest delete = new DeleteRequest();
        delete.setKey(key);
        delegate.delete(delete);

        return Response.noContent().build();
    }
}
