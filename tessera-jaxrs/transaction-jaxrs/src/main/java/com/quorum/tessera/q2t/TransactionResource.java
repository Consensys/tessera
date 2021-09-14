package com.quorum.tessera.q2t;

import static jakarta.ws.rs.core.MediaType.*;

import com.quorum.tessera.api.*;
import com.quorum.tessera.api.constraint.PrivacyValid;
import com.quorum.tessera.config.constraints.ValidBase64;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * <p>- creating new transactions and distributing them - deleting transactions - fetching
 * transactions - resending old transactions
 */
@Tag(name = "quorum-to-tessera")
@Path("/")
public class TransactionResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

  private final TransactionManager transactionManager;

  private final PrivacyGroupManager privacyGroupManager;

  private final Base64.Decoder base64Decoder = Base64.getDecoder();

  private final Base64.Encoder base64Encoder = Base64.getEncoder();

  public TransactionResource(
      TransactionManager transactionManager, PrivacyGroupManager privacyGroupManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.privacyGroupManager = Objects.requireNonNull(privacyGroupManager);
  }

  // hide this operation from swagger generation; the /send operation is overloaded and must be
  // documented in a single
  // place
  @Hidden
  @POST
  @Path("send")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response send(@NotNull @Valid @PrivacyValid final SendRequest sendRequest) {

    final PublicKey sender =
        Optional.ofNullable(sendRequest.getFrom())
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .orElseGet(transactionManager::defaultPublicKey);

    final Optional<PrivacyGroup.Id> optionalPrivacyGroup =
        Optional.ofNullable(sendRequest.getPrivacyGroupId()).map(PrivacyGroup.Id::fromBase64String);

    final List<PublicKey> recipientList =
        optionalPrivacyGroup
            .map(privacyGroupManager::retrievePrivacyGroup)
            .map(PrivacyGroup::getMembers)
            .orElse(
                Stream.of(sendRequest)
                    .filter(sr -> Objects.nonNull(sr.getTo()))
                    .flatMap(s -> Stream.of(s.getTo()))
                    .map(base64Decoder::decode)
                    .map(PublicKey::from)
                    .collect(Collectors.toList()));

    final Set<MessageHash> affectedTransactions =
        Stream.ofNullable(sendRequest.getAffectedContractTransactions())
            .flatMap(Arrays::stream)
            .map(base64Decoder::decode)
            .map(MessageHash::new)
            .collect(Collectors.toSet());

    final byte[] execHash =
        Optional.ofNullable(sendRequest.getExecHash()).map(String::getBytes).orElse(new byte[0]);

    final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendRequest.getPrivacyFlag());

    final com.quorum.tessera.transaction.SendRequest.Builder requestBuilder =
        com.quorum.tessera.transaction.SendRequest.Builder.create()
            .withRecipients(recipientList)
            .withSender(sender)
            .withPayload(sendRequest.getPayload())
            .withExecHash(execHash)
            .withPrivacyMode(privacyMode)
            .withAffectedContractTransactions(affectedTransactions);
    optionalPrivacyGroup.ifPresent(requestBuilder::withPrivacyGroupId);

    final com.quorum.tessera.transaction.SendResponse response =
        transactionManager.send(requestBuilder.build());

    final String encodedKey =
        Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .get();

    final SendResponse sendResponse =
        Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .map(messageHash -> new SendResponse(messageHash, null, null))
            .get();

    final URI location =
        UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(encodedKey, StandardCharsets.UTF_8))
            .build();

    return Response.status(Response.Status.CREATED)
        .type(APPLICATION_JSON)
        .location(location)
        .entity(sendResponse)
        .build();
  }

  // hide this operation from swagger generation; the /sendsignedtx operation is overloaded and must
  // be documented in
  // a single place
  @Hidden
  @POST
  @Path("sendsignedtx")
  @Consumes(APPLICATION_OCTET_STREAM)
  @Produces(TEXT_PLAIN)
  public Response sendSignedTransactionStandard(
      @Parameter(
              description =
                  "comma-separated list of recipient public keys (for application/octet-stream requests)",
              schema = @Schema(format = "base64"))
          @HeaderParam("c11n-to")
          final String recipientKeys,
      @Valid @NotNull @Size(min = 1) final byte[] signedTransaction) {

    final List<PublicKey> recipients =
        Stream.ofNullable(recipientKeys)
            .filter(s -> !Objects.equals("", s))
            .map(v -> v.split(","))
            .flatMap(Arrays::stream)
            .map(base64Decoder::decode)
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

    final com.quorum.tessera.transaction.SendResponse response =
        transactionManager.sendSignedTransaction(request);

    final String encodedTransactionHash =
        base64Encoder.encodeToString(response.getTransactionHash().getHashBytes());

    LOGGER.debug("Encoded key: {}", encodedTransactionHash);

    URI location =
        UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8))
            .build();

    // TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
    return Response.status(Response.Status.OK)
        .entity(encodedTransactionHash)
        .location(location)
        .build();
  }

  // hide this operation from swagger generation; the /sendsignedtx operation is overloaded and must
  // be documented in
  // a single place
  @Hidden
  @POST
  @Path("sendsignedtx")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response sendSignedTransactionEnhanced(
      @NotNull @Valid @PrivacyValid final SendSignedRequest sendSignedRequest) {

    final Optional<PrivacyGroup.Id> privacyGroupId =
        Optional.ofNullable(sendSignedRequest.getPrivacyGroupId())
            .map(PrivacyGroup.Id::fromBase64String);

    final List<PublicKey> recipients =
        privacyGroupId
            .map(privacyGroupManager::retrievePrivacyGroup)
            .map(PrivacyGroup::getMembers)
            .orElse(
                Optional.ofNullable(sendSignedRequest.getTo())
                    .map(Arrays::stream)
                    .orElse(Stream.empty())
                    .map(base64Decoder::decode)
                    .map(PublicKey::from)
                    .collect(Collectors.toList()));

    final PrivacyMode privacyMode = PrivacyMode.fromFlag(sendSignedRequest.getPrivacyFlag());

    final Set<MessageHash> affectedTransactions =
        Stream.ofNullable(sendSignedRequest.getAffectedContractTransactions())
            .flatMap(Arrays::stream)
            .map(base64Decoder::decode)
            .map(MessageHash::new)
            .collect(Collectors.toSet());

    final byte[] execHash =
        Optional.ofNullable(sendSignedRequest.getExecHash())
            .map(String::getBytes)
            .orElse(new byte[0]);

    final com.quorum.tessera.transaction.SendSignedRequest.Builder requestBuilder =
        com.quorum.tessera.transaction.SendSignedRequest.Builder.create()
            .withSignedData(sendSignedRequest.getHash())
            .withRecipients(recipients)
            .withPrivacyMode(privacyMode)
            .withAffectedContractTransactions(affectedTransactions)
            .withExecHash(execHash);
    privacyGroupId.ifPresent(requestBuilder::withPrivacyGroupId);

    final com.quorum.tessera.transaction.SendResponse response =
        transactionManager.sendSignedTransaction(requestBuilder.build());

    final String endcodedTransactionHash =
        Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .get();

    LOGGER.debug("Encoded key: {}", endcodedTransactionHash);

    URI location =
        UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(endcodedTransactionHash, StandardCharsets.UTF_8))
            .build();

    SendResponse sendResponse = new SendResponse();
    sendResponse.setKey(endcodedTransactionHash);

    return Response.status(Response.Status.CREATED)
        .type(APPLICATION_JSON)
        .location(location)
        .entity(sendResponse)
        .build();
  }

  @Operation(
      summary = "/sendraw",
      operationId = "encryptStoreAndSendOctetStream",
      description =
          "encrypts a payload, stores result in database, and publishes result to recipients")
  @ApiResponse(
      responseCode = "200",
      description = "encrypted payload hash",
      content =
          @Content(
              schema =
                  @Schema(
                      type = "string",
                      format = "base64",
                      description = "encrypted payload hash")))
  @POST
  @Path("sendraw")
  @Consumes(APPLICATION_OCTET_STREAM)
  @Produces(TEXT_PLAIN)
  public Response sendRaw(
      @HeaderParam("c11n-from")
          @Parameter(
              description =
                  "public key identifying the server's key pair that will be used in the encryption; if not set, default used",
              schema = @Schema(format = "base64"))
          @Valid
          @ValidBase64
          final String sender,
      @HeaderParam("c11n-to")
          @Parameter(
              description = "comma-separated list of recipient public keys",
              schema = @Schema(format = "base64"))
          final String recipientKeys,
      @Schema(description = "data to be encrypted") @NotNull @Size(min = 1) @Valid
          final byte[] payload) {

    final PublicKey senderKey =
        Optional.ofNullable(sender)
            .filter(Predicate.not(String::isEmpty))
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .orElseGet(transactionManager::defaultPublicKey);

    final List<PublicKey> recipients =
        Stream.of(recipientKeys)
            .filter(Objects::nonNull)
            .filter(s -> !Objects.equals("", s))
            .map(v -> v.split(","))
            .flatMap(Arrays::stream)
            .map(base64Decoder::decode)
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

    final com.quorum.tessera.transaction.SendResponse sendResponse =
        transactionManager.send(request);

    final String encodedTransactionHash =
        Optional.of(sendResponse)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .get();

    LOGGER.debug("Encoded key: {}", encodedTransactionHash);

    URI location =
        UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8))
            .build();

    // TODO: Quorum expects only 200 responses. When Quorum can handle a 201, change to CREATED
    return Response.status(Response.Status.OK)
        .entity(encodedTransactionHash)
        .location(location)
        .build();
  }

  // hide this operation from swagger generation; the /transaction/{hash} operation is overloaded
  // and must be
  // documented in a single place
  @Hidden
  @GET
  @Path("/transaction/{hash}")
  @Produces(APPLICATION_JSON)
  public Response receive(
      @Parameter(
              description = "hash indicating encrypted payload to retrieve from database",
              schema = @Schema(format = "base64"))
          @Valid
          @ValidBase64
          @PathParam("hash")
          final String hash,
      @Parameter(
              description =
                  "(optional) public key of recipient of the encrypted payload; used in decryption; if not provided, decryption is attempted with all known recipient keys in turn",
              schema = @Schema(format = "base64"))
          @QueryParam("to")
          final String toStr,
      @Parameter(
              description =
                  "(optional) indicates whether the payload is raw; determines which database the payload is retrieved from; possible values\n* true - for pre-stored payloads in the \"raw\" database\n* false (default) - for already sent payloads in \"standard\" database")
          @Valid
          @Pattern(flags = Pattern.Flag.CASE_INSENSITIVE, regexp = "^(true|false)$")
          @QueryParam("isRaw")
          final String isRaw) {

    final PublicKey recipient =
        Optional.ofNullable(toStr)
            .filter(Predicate.not(String::isEmpty))
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .orElse(null);

    final MessageHash transactionHash =
        Optional.of(hash).map(base64Decoder::decode).map(MessageHash::new).get();

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
            .map(base64Encoder::encodeToString)
            .toArray(String[]::new));

    Optional.ofNullable(response.getExecHash())
        .map(String::new)
        .ifPresent(receiveResponse::setExecHash);

    receiveResponse.setPrivacyFlag(response.getPrivacyMode().getPrivacyFlag());

    response
        .getPrivacyGroupId()
        .map(PrivacyGroup.Id::getBase64)
        .ifPresent(receiveResponse::setPrivacyGroupId);

    return Response.status(Response.Status.OK)
        .type(APPLICATION_JSON)
        .entity(receiveResponse)
        .build();
  }

  @Operation(
      summary = "/receiveraw",
      operationId = "getDecryptedPayloadOctetStream",
      description = "get payload from database, decrypt, and return")
  @ApiResponse(
      responseCode = "200",
      description = "decrypted ciphertext payload",
      content =
          @Content(
              array =
                  @ArraySchema(
                      schema =
                          @Schema(
                              type = "string",
                              format = "byte",
                              description = "decrypted ciphertext payload"))))
  @GET
  @Path("receiveraw")
  @Consumes(APPLICATION_OCTET_STREAM)
  @Produces(APPLICATION_OCTET_STREAM)
  public Response receiveRaw(
      @Schema(
              description = "hash indicating encrypted payload to retrieve from database",
              format = "base64")
          @ValidBase64
          @NotNull
          @HeaderParam(value = "c11n-key")
          String hash,
      @Schema(
              description =
                  "(optional) public key of recipient of the encrypted payload; used in decryption; if not provided, decryption is attempted with all known recipient keys in turn",
              format = "base64")
          @ValidBase64
          @HeaderParam(value = "c11n-to")
          String recipientKey) {

    LOGGER.debug("Received receiveraw request for hash : {}, recipientKey: {}", hash, recipientKey);

    MessageHash transactionHash =
        Optional.of(hash).map(base64Decoder::decode).map(MessageHash::new).get();
    PublicKey recipient =
        Optional.ofNullable(recipientKey)
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .orElse(null);
    com.quorum.tessera.transaction.ReceiveRequest request =
        com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
            .withTransactionHash(transactionHash)
            .withRecipient(recipient)
            .build();

    com.quorum.tessera.transaction.ReceiveResponse receiveResponse =
        transactionManager.receive(request);

    byte[] payload = receiveResponse.getUnencryptedTransactionData();

    return Response.status(Response.Status.OK).entity(payload).build();
  }

  @Deprecated
  @Operation(
      summary = "/delete",
      operationId = "deleteDeprecated",
      description = "delete payload from database")
  @ApiResponse(
      responseCode = "200",
      description = "delete successful",
      content =
          @Content(
              schema = @Schema(type = "string"),
              examples = @ExampleObject(value = "Delete successful")))
  @POST
  @Path("delete")
  @Consumes(APPLICATION_JSON)
  @Produces(TEXT_PLAIN)
  public Response delete(@Valid final DeleteRequest deleteRequest) {

    LOGGER.debug("Received deprecated delete request");

    MessageHash messageHash =
        Optional.of(deleteRequest)
            .map(DeleteRequest::getKey)
            .map(base64Decoder::decode)
            .map(MessageHash::new)
            .get();

    transactionManager.delete(messageHash);

    return Response.status(Response.Status.OK).entity("Delete successful").build();
  }
}
