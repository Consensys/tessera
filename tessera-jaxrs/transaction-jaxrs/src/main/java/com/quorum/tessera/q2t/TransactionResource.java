package com.quorum.tessera.q2t;

import com.quorum.tessera.api.*;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.*;
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
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public TransactionResource() {
        this(ServiceFactory.create().transactionManager());
    }

    public TransactionResource(TransactionManager transactionManager) {
        this.transactionManager = Objects.requireNonNull(transactionManager);
    }

    @ApiOperation(value = "Send private transaction payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @Path("send")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response send(@ApiParam(name = "sendRequest", required = true) @NotNull @Valid final SendRequest sendRequest)
            throws UnsupportedEncodingException {

        Base64.Decoder base64Decoder = Base64.getDecoder();

        PublicKey sender = Optional.ofNullable(sendRequest.getFrom())
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .orElse(transactionManager.defaultPublicKey());

        final byte[][] recipients =
            Stream.of(sendRequest)
                .filter(sr -> Objects.nonNull(sr.getTo()))
                .flatMap(s -> Stream.of(s.getTo()))
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        final List<PublicKey> recipientList = Stream.of(recipients).map(PublicKey::from).collect(Collectors.toList());

        com.quorum.tessera.transaction.SendRequest request = com.quorum.tessera.transaction.SendRequest.Builder.create()
            .withRecipients(recipientList)
            .withSender(sender)
            .withPayload(sendRequest.getPayload())
            .build();

        final com.quorum.tessera.transaction.SendResponse response = transactionManager.send(request);

        final String encodedKey = Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(Base64.getEncoder()::encodeToString)
            .get();


        final SendResponse sendResponse = Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(Base64.getEncoder()::encodeToString).map(SendResponse::new).get();

        final URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(encodedKey, StandardCharsets.UTF_8.toString()))
                        .build();

        return Response
                .status(Status.CREATED)
                .type(APPLICATION_JSON)
                .location(location)
                .entity(sendResponse)
                .build();
    }

    @ApiOperation(value = "Send private raw transaction payload")
    @ApiResponses({
        @ApiResponse(code = 200, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @Path("sendsignedtx")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(TEXT_PLAIN)
    public Response sendSignedTransaction(
            @HeaderParam("c11n-to") final String recipientKeys, @NotNull @Size(min = 1) final byte[] signedTransaction)
            throws UnsupportedEncodingException {


        List<PublicKey> recipients = Stream.of(recipientKeys)
            .filter(Objects::nonNull)
            .filter(s -> !Objects.equals("", s))
            .map(v -> v.split(","))
            .flatMap(Arrays::stream)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .collect(Collectors.toList());

        com.quorum.tessera.transaction.SendSignedRequest request = com.quorum.tessera.transaction.SendSignedRequest.Builder.create()
            .withRecipients(recipients)
            .withSignedData(signedTransaction)
            .build();

        final com.quorum.tessera.transaction.SendResponse response = transactionManager.sendSignedTransaction(request);

        final String encodedTransactionHash = Base64.getEncoder().encodeToString(response.getTransactionHash().getHashBytes());

        LOGGER.debug("Encoded key: {}", encodedTransactionHash);

        URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8.toString()))
                        .build();

        // TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Status.OK).entity(encodedTransactionHash).location(location).build();
    }

    @ApiOperation(value = "Send private transaction payload")
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
            @NotNull @Size(min = 1) final byte[] payload)
            throws UnsupportedEncodingException {

        PublicKey senderKey = Optional.of(sender)
            .filter(Objects::nonNull)
            .filter(Predicate.not(String::isEmpty))
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .orElse(transactionManager.defaultPublicKey());

        List<PublicKey> receipents =
            Stream.of(recipientKeys)
             .filter(Objects::nonNull)
            .filter(s -> !Objects.equals("", s))
            .map(v -> v.split(","))
            .flatMap(Arrays::stream)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from).collect(Collectors.toList());



        com.quorum.tessera.transaction.SendRequest request = com.quorum.tessera.transaction.SendRequest.Builder.create()
            .withSender(senderKey)
            .withRecipients(receipents)
            .withPayload(payload)
            .build();

        final com.quorum.tessera.transaction.SendResponse sendResponse = transactionManager.send(request);

        final String encodedTransactionHash =
            Optional.of(sendResponse)
                .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
                .map(MessageHash::getHashBytes)
                .map(Base64.getEncoder()::encodeToString).get();

        LOGGER.debug("Encoded key: {}", encodedTransactionHash);

        URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8.toString()))
                        .build();

        // TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Status.OK).entity(encodedTransactionHash).location(location).build();
    }

    @ApiOperation(value = "Returns decrypted payload back to Quorum")
    @ApiResponses({@ApiResponse(code = 200, response = ReceiveResponse.class, message = "Receive Response object")})
    @GET
    @Path("/transaction/{hash}")
    @Produces(APPLICATION_JSON)
    public Response receive(
            @ApiParam("Encoded hash used to decrypt the payload") @NotNull @Valid @PathParam("hash") final String hash,
            @ApiParam("Encoded recipient key") @Valid @QueryParam("to") final String toStr) {

        Base64.Decoder base64Decoder = Base64.getDecoder();
        PublicKey recipient = Optional.ofNullable(toStr)
            .filter(Predicate.not(String::isEmpty))
            .map(base64Decoder::decode)
            .map(PublicKey::from).orElse(null);

        MessageHash transactionHash = Optional.of(hash)
            .map(base64Decoder::decode)
            .map(MessageHash::new).get();

        com.quorum.tessera.transaction.ReceiveRequest request = com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
            .withRecipient(recipient)
            .withTransactionHash(transactionHash)
            .build();

        com.quorum.tessera.transaction.ReceiveResponse response = transactionManager.receive(request);

        ReceiveResponse receiveResponse = new ReceiveResponse();
        receiveResponse.setPayload(response.getUnencryptedTransactionData());

        return Response
                .status(Status.OK)
                .type(APPLICATION_JSON)
                .entity(receiveResponse)
                .build();
    }

    @GET
    @Path("/receive")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response receive(@Valid final ReceiveRequest request) {

        LOGGER.debug("Received receive request");

        Base64.Decoder decoder = Base64.getDecoder();

        MessageHash transactionHash = Optional.of(request)
            .map(ReceiveRequest::getKey)
            .map(decoder::decode)
            .map(MessageHash::new)
            .get();


        PublicKey recipient = Optional.of(request)
            .map(ReceiveRequest::getTo)
            .filter(str -> !str.isEmpty())
            .map(decoder::decode)
            .map(PublicKey::from)
            .orElse(null);

        com.quorum.tessera.transaction.ReceiveRequest receiveRequest =
            com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
                .withTransactionHash(transactionHash)
                .withRecipient(recipient)
                .build();

        com.quorum.tessera.transaction.ReceiveResponse response = transactionManager.receive(receiveRequest);

        ReceiveResponse receiveResponse = Optional.of(response)
            .map(com.quorum.tessera.transaction.ReceiveResponse::getUnencryptedTransactionData)
            .map(ReceiveResponse::new)
            .get();

        return Response.status(Status.OK).type(APPLICATION_JSON).entity(receiveResponse).build();
    }

    @ApiOperation(value = "Submit keys to retrieve payload and decrypt it")
    @ApiResponses({@ApiResponse(code = 200, message = "Raw payload", response = byte[].class)})
    @GET
    @Path("receiveraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response receiveRaw(
            @ApiParam("Encoded transaction hash") @NotNull @HeaderParam(value = "c11n-key") String hash,
            @ApiParam("Encoded Recipient Public Key") @HeaderParam(value = "c11n-to") String recipientKey) {

        LOGGER.debug("Received receiveraw request for hash : {}, recipientKey: {}", hash, recipientKey);

        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(hash);
        receiveRequest.setTo(recipientKey);
        MessageHash transactionHash = Optional.of(hash).map(Base64.getDecoder()::decode).map(MessageHash::new).get();
        PublicKey recipient = Optional.of(recipientKey).map(Base64.getDecoder()::decode).map(PublicKey::from).get();
        com.quorum.tessera.transaction.ReceiveRequest request = com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
            .withTransactionHash(transactionHash)
            .withRecipient(recipient)
            .build();

        com.quorum.tessera.transaction.ReceiveResponse receiveResponse = transactionManager.receive(request);

        byte[] payload = receiveResponse.getUnencryptedTransactionData();

        return Response.status(Status.OK).entity(payload).build();
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
            @ApiParam(name = "deleteRequest", required = true, value = "Key data to be deleted") @Valid
                    final DeleteRequest deleteRequest) {

        LOGGER.debug("Received deprecated delete request");

        MessageHash messageHash = Optional.of(deleteRequest).map(DeleteRequest::getKey)
            .map(Base64.getDecoder()::decode).map(MessageHash::new).get();

        transactionManager.delete(messageHash);

        return Response.status(Response.Status.OK).entity("Delete successful").build();
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

        Base64.Decoder base64Decoder = Base64.getDecoder();
        MessageHash messageHash = new MessageHash(base64Decoder.decode(key));

        transactionManager.delete(messageHash);

        return Response.noContent().build();
    }

    @GET
    @Path("/transaction/{key}/isSender")
    public Response isSender(@ApiParam("Encoded hash") @PathParam("key") final String ptmHash) {

        LOGGER.debug("Received isSender API request for key {}", ptmHash);

        boolean isSender = transactionManager.isSender(ptmHash);

        return Response.ok(isSender).build();
    }

    @GET
    @Path("/transaction/{key}/participants")
    public Response getParticipants(@ApiParam("Encoded hash") @PathParam("key") final String ptmHash) {

        LOGGER.debug("Received participants list API request for key {}", ptmHash);

        final String participantList =
                transactionManager.getParticipants(ptmHash).stream()
                        .map(PublicKey::encodeToBase64)
                        .collect(Collectors.joining(","));

        return Response.ok(participantList).build();
    }
}
