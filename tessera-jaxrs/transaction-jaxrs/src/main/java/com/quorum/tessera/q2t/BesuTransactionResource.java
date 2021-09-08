package com.quorum.tessera.q2t;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import com.quorum.tessera.api.*;
import com.quorum.tessera.api.constraint.PrivacyValid;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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

@Tag(name = "quorum-to-tessera")
@Path("/")
public class BesuTransactionResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

  private static final String ORION = "application/vnd.orion.v1+json";

  private final TransactionManager transactionManager;

  private final PrivacyGroupManager privacyGroupManager;

  private final Base64.Decoder base64Decoder = Base64.getDecoder();

  private final Base64.Encoder base64Encoder = Base64.getEncoder();

  public BesuTransactionResource(
      TransactionManager transactionManager, PrivacyGroupManager privacyGroupManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.privacyGroupManager = Objects.requireNonNull(privacyGroupManager);
  }

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

    optionalPrivacyGroup.ifPresentOrElse(
        requestBuilder::withPrivacyGroupId,
        () -> {
          PrivacyGroup legacyGroup =
              privacyGroupManager.createLegacyPrivacyGroup(sender, recipientList);
          requestBuilder.withPrivacyGroupId(legacyGroup.getId());
        });

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

    return Response.status(Response.Status.OK)
        .type(APPLICATION_JSON)
        .location(location)
        .entity(sendResponse)
        .build();
  }

  @Operation(
      summary = "/receive",
      operationId = "getDecryptedPayloadJson",
      description =
          "get payload from database, decrypt, and return. This endpoint is only to be used by Besu")
  @ApiResponse(
      responseCode = "200",
      description = "decrypted payload",
      content = {
        @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = BesuReceiveResponse.class)),
        @Content(mediaType = ORION, schema = @Schema(implementation = BesuReceiveResponse.class))
      })
  @POST
  @Path("/receive")
  @Consumes({APPLICATION_JSON, ORION})
  @Produces(APPLICATION_JSON)
  public Response receive(@Valid final ReceiveRequest request) {

    LOGGER.debug("Received receive request");

    MessageHash transactionHash =
        Optional.of(request)
            .map(ReceiveRequest::getKey)
            .map(base64Decoder::decode)
            .map(MessageHash::new)
            .get();

    PublicKey recipient =
        Optional.of(request)
            .map(ReceiveRequest::getTo)
            .filter(Predicate.not(String::isEmpty))
            .filter(Objects::nonNull)
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .orElse(null);

    com.quorum.tessera.transaction.ReceiveRequest receiveRequest =
        com.quorum.tessera.transaction.ReceiveRequest.Builder.create()
            .withTransactionHash(transactionHash)
            .withRecipient(recipient)
            .withRaw(request.isRaw())
            .build();

    com.quorum.tessera.transaction.ReceiveResponse response =
        transactionManager.receive(receiveRequest);

    BesuReceiveResponse receiveResponse = new BesuReceiveResponse();
    receiveResponse.setPayload(response.getUnencryptedTransactionData());
    receiveResponse.setSenderKey(response.sender().encodeToBase64());
    response
        .getPrivacyGroupId()
        .map(PrivacyGroup.Id::getBase64)
        .ifPresent(receiveResponse::setPrivacyGroupId);

    return Response.status(Response.Status.OK)
        .type(APPLICATION_JSON)
        .entity(receiveResponse)
        .build();
  }
}
