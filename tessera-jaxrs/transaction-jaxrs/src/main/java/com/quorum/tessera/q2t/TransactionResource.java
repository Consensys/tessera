package com.quorum.tessera.q2t;

import com.quorum.tessera.api.*;
import com.quorum.tessera.api.constraint.PrivacyValid;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
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
    public Response send(
        @ApiParam(name = "sendRequest", required = true) @NotNull @Valid @PrivacyValid final SendRequest sendRequest
    ) {

        Base64.Decoder base64Decoder = Base64.getDecoder();

        PublicKey sender =
                Optional.ofNullable(sendRequest.getFrom())
                        .map(base64Decoder::decode)
                        .map(PublicKey::from)
                        .orElseGet(transactionManager::defaultPublicKey);

        final List<PublicKey> recipientList =
                Stream.of(sendRequest)
                        .filter(sr -> Objects.nonNull(sr.getTo()))
                        .flatMap(s -> Stream.of(s.getTo()))
                        .map(base64Decoder::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final Set<MessageHash> affectedTransactions =
                Stream.ofNullable(sendRequest.getAffectedContractTransactions())
                        .flatMap(Arrays::stream)
                        .map(Base64.getDecoder()::decode)
                        .map(MessageHash::new)
                        .collect(Collectors.toSet());

        final byte[] execHash =
                Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendRequest.getPrivacyFlag());

        final com.quorum.tessera.transaction.SendRequest request =
                com.quorum.tessera.transaction.SendRequest.Builder.create()
                        .withRecipients(recipientList)
                        .withSender(sender)
                        .withPayload(sendRequest.getPayload())
                        .withExecHash(execHash)
                        .withPrivacyMode(privacyMode)
                        .withAffectedContractTransactions(affectedTransactions)
                        .build();

        final com.quorum.tessera.transaction.SendResponse response = transactionManager.send(request);

        final String encodedKey =
                Optional.of(response)
                        .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
                        .map(MessageHash::getHashBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .get();

        final SendResponse sendResponse =
                Optional.of(response)
                        .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
                        .map(MessageHash::getHashBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .map(SendResponse::new)
                        .get();

        final URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(encodedKey, StandardCharsets.UTF_8))
                        .build();

        return Response.status(Status.CREATED).type(APPLICATION_JSON).location(location).entity(sendResponse).build();
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
            @HeaderParam("c11n-to") final String recipientKeys,
            @Valid @NotNull @Size(min = 1) final byte[] signedTransaction) {

        final List<PublicKey> recipients =
                Stream.ofNullable(recipientKeys)
                        .filter(s -> !Objects.equals("", s))
                        .map(v -> v.split(","))
                        .flatMap(Arrays::stream)
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final com.quorum.tessera.transaction.SendSignedRequest request =
                com.quorum.tessera.transaction.SendSignedRequest.Builder.create()
                        .withRecipients(recipients)
                        .withSignedData(signedTransaction)
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedContractTransactions(Collections.emptySet())
                        .withExecHash(new byte[0])
                        .build();

        final com.quorum.tessera.transaction.SendResponse response = transactionManager.sendSignedTransaction(request);

        final String encodedTransactionHash =
                Base64.getEncoder().encodeToString(response.getTransactionHash().getHashBytes());

        LOGGER.debug("Encoded key: {}", encodedTransactionHash);

        URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8))
                        .build();

        // TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
        return Response.status(Status.OK).entity(encodedTransactionHash).location(location).build();
    }

    @ApiOperation(value = "Send private raw transaction payload", produces = "Encrypted payload hash")
    @ApiResponses({
        @ApiResponse(code = 201, response = SendResponse.class, message = "Send response"),
        @ApiResponse(code = 400, message = "For unknown and unknown keys")
    })
    @POST
    @Path("sendsignedtx")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response sendSignedTransaction(
            @ApiParam(name = "sendSignedRequest", required = true) @NotNull @Valid @PrivacyValid
                    final SendSignedRequest sendSignedRequest) {

        final List<PublicKey> recipients =
                Optional.ofNullable(sendSignedRequest.getTo())
                        .map(Arrays::stream)
                        .orElse(Stream.empty())
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendSignedRequest.getPrivacyFlag());

        final Set<MessageHash> affectedTransactions =
                Stream.ofNullable(sendSignedRequest.getAffectedContractTransactions())
                        .flatMap(Arrays::stream)
                        .map(Base64.getDecoder()::decode)
                        .map(MessageHash::new)
                        .collect(Collectors.toSet());

        final byte[] execHash =
                Optional.ofNullable(sendSignedRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

        final com.quorum.tessera.transaction.SendSignedRequest request =
                com.quorum.tessera.transaction.SendSignedRequest.Builder.create()
                        .withSignedData(sendSignedRequest.getHash())
                        .withRecipients(recipients)
                        .withPrivacyMode(privacyMode)
                        .withAffectedContractTransactions(affectedTransactions)
                        .withExecHash(execHash)
                        .build();

        final com.quorum.tessera.transaction.SendResponse response = transactionManager.sendSignedTransaction(request);

        final String endcodedTransactionHash =
                Optional.of(response)
                        .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
                        .map(MessageHash::getHashBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .get();

        LOGGER.debug("Encoded key: {}", endcodedTransactionHash);

        URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(endcodedTransactionHash, StandardCharsets.UTF_8))
                        .build();

        SendResponse sendResponse = new SendResponse();
        sendResponse.setKey(endcodedTransactionHash);

        return Response.status(Status.CREATED).type(APPLICATION_JSON).location(location).entity(sendResponse).build();
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
            @HeaderParam("c11n-from") @Valid @ValidBase64 final String sender,
            @HeaderParam("c11n-to") final String recipientKeys,
            @NotNull @Size(min = 1) @Valid final byte[] payload) {

        final PublicKey senderKey =
                Optional.ofNullable(sender)
                        .filter(Predicate.not(String::isEmpty))
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .orElseGet(transactionManager::defaultPublicKey);

        final List<PublicKey> recipients =
                Stream.of(recipientKeys)
                        .filter(Objects::nonNull)
                        .filter(s -> !Objects.equals("", s))
                        .map(v -> v.split(","))
                        .flatMap(Arrays::stream)
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        final com.quorum.tessera.transaction.SendRequest request =
                com.quorum.tessera.transaction.SendRequest.Builder.create()
                        .withSender(senderKey)
                        .withRecipients(recipients)
                        .withPayload(payload)
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedContractTransactions(Collections.emptySet())
                        .withExecHash(new byte[0])
                        .build();

        final com.quorum.tessera.transaction.SendResponse sendResponse = transactionManager.send(request);

        final String encodedTransactionHash =
                Optional.of(sendResponse)
                        .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
                        .map(MessageHash::getHashBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .get();

        LOGGER.debug("Encoded key: {}", encodedTransactionHash);

        URI location =
                UriBuilder.fromPath("transaction")
                        .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8))
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
            @ApiParam("Encoded hash used to decrypt the payload") @Valid @ValidBase64 @PathParam("hash")
                    final String hash,
            @ApiParam("Encoded recipient key") @QueryParam("to") final String toStr,
            @ApiParam("isRaw flag")
                    @Valid
                    @Pattern(flags = Pattern.Flag.CASE_INSENSITIVE, regexp = "^(true|false)$")
                    @QueryParam("isRaw")
                    final String isRaw) {

        Base64.Decoder base64Decoder = Base64.getDecoder();
        final PublicKey recipient =
                Optional.ofNullable(toStr)
                        .filter(Predicate.not(String::isEmpty))
                        .map(base64Decoder::decode)
                        .map(PublicKey::from)
                        .orElse(null);

        final MessageHash transactionHash = Optional.of(hash).map(base64Decoder::decode).map(MessageHash::new).get();

        final com.quorum.tessera.transaction.ReceiveRequest request =
                com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
                        .withRecipient(recipient)
                        .withTransactionHash(transactionHash)
                        .withRaw(Boolean.valueOf(isRaw))
                        .build();

        com.quorum.tessera.transaction.ReceiveResponse response = transactionManager.receive(request);

        final ReceiveResponse receiveResponse = new ReceiveResponse();
        receiveResponse.setPayload(response.getUnencryptedTransactionData());
        receiveResponse.setAffectedContractTransactions(
                response.getAffectedTransactions().stream()
                        .map(MessageHash::getHashBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .toArray(String[]::new));

        Optional.ofNullable(response.getExecHash()).map(String::new).ifPresent(receiveResponse::setExecHash);

        receiveResponse.setPrivacyFlag(response.getPrivacyMode().getPrivacyFlag());

        return Response.status(Status.OK).type(APPLICATION_JSON).entity(receiveResponse).build();
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
            .filter(Predicate.not(String::isEmpty))
            .filter(Objects::nonNull)
            .map(decoder::decode)
            .map(PublicKey::from)
            .orElse(null);

        com.quorum.tessera.transaction.ReceiveRequest receiveRequest =
                com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
                        .withTransactionHash(transactionHash)
                        .withRecipient(recipient)
                        .withRaw(request.isRaw())
                        .build();

        com.quorum.tessera.transaction.ReceiveResponse response = transactionManager.receive(receiveRequest);

        ReceiveResponse receiveResponse = new ReceiveResponse();
        receiveResponse.setPrivacyFlag(response.getPrivacyMode().getPrivacyFlag());
        receiveResponse.setPayload(response.getUnencryptedTransactionData());
        Optional.ofNullable(response.getExecHash()).map(String::new).ifPresent(receiveResponse::setExecHash);

        String[] affectedTransactions =
                response.getAffectedTransactions().stream()
                        .map(MessageHash::getHashBytes)
                        .map(Base64.getEncoder()::encodeToString)
                        .toArray(String[]::new);

        receiveResponse.setAffectedContractTransactions(affectedTransactions);

        return Response.status(Status.OK).type(APPLICATION_JSON).entity(receiveResponse).build();
    }

    @ApiOperation(value = "Submit keys to retrieve payload and decrypt it")
    @ApiResponses({@ApiResponse(code = 200, message = "Raw payload", response = byte[].class)})
    @GET
    @Path("receiveraw")
    @Consumes(APPLICATION_OCTET_STREAM)
    @Produces(APPLICATION_OCTET_STREAM)
    public Response receiveRaw(
            @ApiParam("Encoded transaction hash") @ValidBase64 @NotNull @HeaderParam(value = "c11n-key") String hash,
            @ApiParam("Encoded Recipient Public Key") @ValidBase64 @HeaderParam(value = "c11n-to")
                    String recipientKey) {

        LOGGER.debug("Received receiveraw request for hash : {}, recipientKey: {}", hash, recipientKey);

        MessageHash transactionHash = Optional.of(hash).map(Base64.getDecoder()::decode).map(MessageHash::new).get();
        PublicKey recipient =
                Optional.ofNullable(recipientKey).map(Base64.getDecoder()::decode).map(PublicKey::from).orElse(null);
        com.quorum.tessera.transaction.ReceiveRequest request =
                com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
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

        MessageHash messageHash =
                Optional.of(deleteRequest)
                        .map(DeleteRequest::getKey)
                        .map(Base64.getDecoder()::decode)
                        .map(MessageHash::new)
                        .get();

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

        MessageHash transactionHash = Optional.of(ptmHash)
            .map(Base64.getDecoder()::decode)
            .map(MessageHash::new).get();

        boolean isSender = transactionManager.isSender(transactionHash);

        return Response.ok(isSender).build();
    }

    @GET
    @Path("/transaction/{key}/participants")
    public Response getParticipants(@ApiParam("Encoded hash") @PathParam("key") final String ptmHash) {

        LOGGER.debug("Received participants list API request for key {}", ptmHash);

        MessageHash transactionHash = Optional.of(ptmHash)
            .map(Base64.getDecoder()::decode)
            .map(MessageHash::new).get();

        final String participantList =
                transactionManager.getParticipants(transactionHash).stream()
                        .map(PublicKey::encodeToBase64)
                        .collect(Collectors.joining(","));

        return Response.ok(participantList).build();
    }
}
