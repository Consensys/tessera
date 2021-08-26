package com.quorum.tessera.q2t;

import static com.quorum.tessera.version.MandatoryRecipientsVersion.MIME_TYPE_JSON_4;

import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.api.SendResponse;
import com.quorum.tessera.api.SendSignedRequest;
import com.quorum.tessera.api.constraint.PrivacyValid;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides endpoints for dealing with transactions, including:
 *
 * <p>- creating new transactions and distributing them - deleting transactions - fetching
 * transactions - resending old transactions
 *
 * <p>This resources deal with send, sendsignedtx, and receive for mime type
 * application/vnd.tessera-4.0+json
 */
@Tag(name = "quorum-to-tessera")
@Path("/")
public class TransactionResource4 {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource4.class);

  private final TransactionManager transactionManager;

  private final PrivacyGroupManager privacyGroupManager;

  private final Base64.Decoder base64Decoder = Base64.getDecoder();

  private final Base64.Encoder base64Encoder = Base64.getEncoder();

  public TransactionResource4(
      final TransactionManager transactionManager, final PrivacyGroupManager privacyGroupManager) {
    this.transactionManager = Objects.requireNonNull(transactionManager);
    this.privacyGroupManager = Objects.requireNonNull(privacyGroupManager);
  }

  @POST
  @Path("send")
  @Consumes({MIME_TYPE_JSON_4})
  @Produces({MIME_TYPE_JSON_4})
  public Response send(@NotNull @Valid @PrivacyValid final SendRequest sendRequest) {

    final PublicKey sender =
        Optional.ofNullable(sendRequest.getFrom())
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .orElseGet(transactionManager::defaultPublicKey);

    final Optional<PrivacyGroup.Id> privacyGroupId =
        Optional.ofNullable(sendRequest.getPrivacyGroupId()).map(PrivacyGroup.Id::fromBase64String);

    final List<PublicKey> recipientList =
        privacyGroupId
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

    final Set<PublicKey> mandatoryRecipients =
        Stream.ofNullable(sendRequest.getMandatoryRecipients())
            .flatMap(Arrays::stream)
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .collect(Collectors.toUnmodifiableSet());

    final com.quorum.tessera.transaction.SendRequest.Builder requestBuilder =
        com.quorum.tessera.transaction.SendRequest.Builder.create()
            .withRecipients(recipientList)
            .withSender(sender)
            .withPayload(sendRequest.getPayload())
            .withExecHash(execHash)
            .withPrivacyMode(privacyMode)
            .withAffectedContractTransactions(affectedTransactions)
            .withMandatoryRecipients(mandatoryRecipients);
    privacyGroupId.ifPresent(requestBuilder::withPrivacyGroupId);

    final com.quorum.tessera.transaction.SendResponse response =
        transactionManager.send(requestBuilder.build());

    final String encodedKey =
        Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .get();

    final String[] managedParties =
        Optional.of(response).map(com.quorum.tessera.transaction.SendResponse::getManagedParties)
            .orElse(Collections.emptySet()).stream()
            .map(PublicKey::encodeToBase64)
            .toArray(String[]::new);

    final SendResponse sendResponse =
        Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .map(
                messageHash ->
                    new SendResponse(messageHash, managedParties, sender.encodeToBase64()))
            .get();

    final URI location =
        UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(encodedKey, StandardCharsets.UTF_8))
            .build();

    return Response.created(location).entity(sendResponse).build();
  }

  @POST
  @Path("sendsignedtx")
  @Consumes({MIME_TYPE_JSON_4})
  @Produces({MIME_TYPE_JSON_4})
  public Response sendSignedTransaction(
      @NotNull @Valid @PrivacyValid final SendSignedRequest sendSignedRequest) {

    final Optional<PrivacyGroup.Id> privacyGroupId =
        Optional.ofNullable(sendSignedRequest.getPrivacyGroupId())
            .map(PrivacyGroup.Id::fromBase64String);

    final List<PublicKey> recipients =
        privacyGroupId
            .map(privacyGroupManager::retrievePrivacyGroup)
            .map(PrivacyGroup::getMembers)
            .orElse(
                Optional.ofNullable(sendSignedRequest.getTo()).stream()
                    .flatMap(Arrays::stream)
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

    final Set<PublicKey> mandatoryRecipients =
        Stream.ofNullable(sendSignedRequest.getMandatoryRecipients())
            .flatMap(Arrays::stream)
            .map(base64Decoder::decode)
            .map(PublicKey::from)
            .collect(Collectors.toUnmodifiableSet());

    final com.quorum.tessera.transaction.SendSignedRequest.Builder requestBuilder =
        com.quorum.tessera.transaction.SendSignedRequest.Builder.create()
            .withSignedData(sendSignedRequest.getHash())
            .withRecipients(recipients)
            .withPrivacyMode(privacyMode)
            .withAffectedContractTransactions(affectedTransactions)
            .withExecHash(execHash)
            .withMandatoryRecipients(mandatoryRecipients);
    privacyGroupId.ifPresent(requestBuilder::withPrivacyGroupId);

    final com.quorum.tessera.transaction.SendResponse response =
        transactionManager.sendSignedTransaction(requestBuilder.build());

    final String encodedTransactionHash =
        Optional.of(response)
            .map(com.quorum.tessera.transaction.SendResponse::getTransactionHash)
            .map(MessageHash::getHashBytes)
            .map(base64Encoder::encodeToString)
            .get();

    LOGGER.debug("Encoded key: {}", encodedTransactionHash);

    final URI location =
        UriBuilder.fromPath("transaction")
            .path(URLEncoder.encode(encodedTransactionHash, StandardCharsets.UTF_8))
            .build();

    final String[] managedParties =
        Optional.of(response).map(com.quorum.tessera.transaction.SendResponse::getManagedParties)
            .orElse(Collections.emptySet()).stream()
            .map(PublicKey::encodeToBase64)
            .toArray(String[]::new);

    final SendResponse responseEntity = new SendResponse();
    responseEntity.setKey(encodedTransactionHash);
    responseEntity.setManagedParties(managedParties);
    responseEntity.setSenderKey(response.getSender().encodeToBase64());

    LOGGER.debug("Encoded key: {}", encodedTransactionHash);

    return Response.created(location).entity(responseEntity).build();
  }
}
