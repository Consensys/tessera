package com.quorum.tessera.q2t;

import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import com.quorum.tessera.api.PayloadDecryptRequest;
import com.quorum.tessera.api.PayloadEncryptResponse;
import com.quorum.tessera.api.ReceiveResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.enclave.RecipientBox;
import com.quorum.tessera.enclave.TxHash;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.TransactionManager;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EncodedPayloadResource allows for manipulation of encrypted payloads without having extra
 * functionality attached to it that one would get with the {@see TransactionResource}, such as
 * savings payloads to database and distributing payloads to peers.
 */
@Tag(name = "quorum-to-tessera")
@Path("/encodedpayload")
public class EncodedPayloadResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncodedPayloadResource.class);

  private final Base64.Decoder base64Decoder = Base64.getDecoder();

  private final EncodedPayloadManager encodedPayloadManager;

  private final TransactionManager transactionManager;

  public EncodedPayloadResource(
      final EncodedPayloadManager encodedPayloadManager,
      final TransactionManager transactionManager) {
    this.encodedPayloadManager = Objects.requireNonNull(encodedPayloadManager);
    this.transactionManager = Objects.requireNonNull(transactionManager);
  }

  // hide this operation from swagger generation; the /encodedpayload/create operation is overloaded
  // and must be documented in a single place
  @Hidden
  @POST
  @Path("create")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createEncodedPayload(@NotNull @Valid final SendRequest sendRequest) {
    LOGGER.info("Received request for custom payload encryption");

    final PublicKey sender =
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

    final com.quorum.tessera.transaction.SendRequest request =
        com.quorum.tessera.transaction.SendRequest.Builder.create()
            .withRecipients(recipientList)
            .withSender(sender)
            .withPayload(sendRequest.getPayload())
            .withExecHash(execHash)
            .withPrivacyMode(PrivacyMode.fromFlag(sendRequest.getPrivacyFlag()))
            .withAffectedContractTransactions(affectedTransactions)
            .build();

    final EncodedPayload encodedPayload = encodedPayloadManager.create(request);

    final Map<String, String> affectedContractTransactionMap =
        encodedPayload.getAffectedContractTransactions().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey().encodeToBase64(),
                    e -> Base64.getEncoder().encodeToString(e.getValue().getData())));

    final PayloadEncryptResponse response = new PayloadEncryptResponse();
    response.setSenderKey(encodedPayload.getSenderKey().getKeyBytes());
    response.setCipherText(encodedPayload.getCipherText());
    response.setCipherTextNonce(encodedPayload.getCipherTextNonce().getNonceBytes());
    response.setRecipientBoxes(
        encodedPayload.getRecipientBoxes().stream()
            .map(RecipientBox::getData)
            .collect(Collectors.toList()));
    response.setRecipientNonce(encodedPayload.getRecipientNonce().getNonceBytes());
    response.setRecipientKeys(
        encodedPayload.getRecipientKeys().stream()
            .map(PublicKey::getKeyBytes)
            .collect(Collectors.toList()));
    response.setPrivacyMode(encodedPayload.getPrivacyMode().getPrivacyFlag());
    response.setAffectedContractTransactions(affectedContractTransactionMap);
    response.setExecHash(encodedPayload.getExecHash());

    return Response.ok(response).type(APPLICATION_JSON).build();
  }

  // hide this operation from swagger generation; the /encodedpayload/decrypt operation is
  // overloaded and must be documented in a single place
  @Hidden
  @POST
  @Path("decrypt")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response decryptEncodedPayload(@Valid @NotNull final PayloadDecryptRequest request) {
    LOGGER.info("Received request to decrypt custom transaction");

    final Base64.Decoder decoder = Base64.getDecoder();
    final Map<TxHash, byte[]> affectedTxns =
        request.getAffectedContractTransactions().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> TxHash.from(decoder.decode(e.getKey())),
                    e -> decoder.decode(e.getValue())));

    final EncodedPayload requestAsPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(request.getSenderKey()))
            .withCipherText(request.getCipherText())
            .withCipherTextNonce(request.getCipherTextNonce())
            .withRecipientBoxes(request.getRecipientBoxes())
            .withRecipientNonce(request.getRecipientNonce())
            .withRecipientKeys(
                request.getRecipientKeys().stream()
                    .map(PublicKey::from)
                    .collect(Collectors.toList()))
            .withPrivacyFlag(request.getPrivacyMode())
            .withAffectedContractTransactions(affectedTxns)
            .withExecHash(request.getExecHash())
            .build();

    final com.quorum.tessera.transaction.ReceiveResponse response =
        encodedPayloadManager.decrypt(requestAsPayload, null);

    final ReceiveResponse receiveResponse = new ReceiveResponse();
    receiveResponse.setPrivacyFlag(response.getPrivacyMode().getPrivacyFlag());
    receiveResponse.setPayload(response.getUnencryptedTransactionData());
    receiveResponse.setAffectedContractTransactions(
        response.getAffectedTransactions().stream()
            .map(MessageHash::getHashBytes)
            .map(Base64.getEncoder()::encodeToString)
            .toArray(String[]::new));

    Optional.ofNullable(response.getExecHash())
        .map(String::new)
        .ifPresent(receiveResponse::setExecHash);

    return Response.ok(receiveResponse).type(APPLICATION_JSON).build();
  }

  // path /encodedpayload/create is overloaded (application/json and
  // application/vnd.tessera-2.1+json); swagger annotations cannot handle situations like this so
  // this operation documents both
  @POST
  @Path("create")
  @Operation(
      summary = "/encodedpayload/create",
      operationId = "encrypt",
      description =
          "encrypt a payload and return the result; does not store to the database or push to peers",
      requestBody =
          @RequestBody(
              content = {
                @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = SendRequest.class)),
                @Content(
                    mediaType = MIME_TYPE_JSON_2_1,
                    schema = @Schema(implementation = SendRequest.class))
              }))
  @ApiResponse(
      responseCode = "200",
      description = "encrypted payload",
      content = {
        @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = PayloadEncryptResponse.class)),
        @Content(
            mediaType = MIME_TYPE_JSON_2_1,
            schema = @Schema(implementation = PayloadEncryptResponse.class))
      })
  @Consumes(MIME_TYPE_JSON_2_1)
  @Produces(MIME_TYPE_JSON_2_1)
  public Response createEncodedPayload21(@NotNull @Valid final SendRequest sendRequest) {
    LOGGER.info("Received request for custom payload encryption");

    final PublicKey sender =
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

    final com.quorum.tessera.transaction.SendRequest request =
        com.quorum.tessera.transaction.SendRequest.Builder.create()
            .withRecipients(recipientList)
            .withSender(sender)
            .withPayload(sendRequest.getPayload())
            .withExecHash(execHash)
            .withPrivacyMode(PrivacyMode.fromFlag(sendRequest.getPrivacyFlag()))
            .withAffectedContractTransactions(affectedTransactions)
            .build();

    final EncodedPayload encodedPayload = encodedPayloadManager.create(request);

    final Map<String, String> affectedContractTransactionMap =
        encodedPayload.getAffectedContractTransactions().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey().encodeToBase64(),
                    e -> Base64.getEncoder().encodeToString(e.getValue().getData())));

    final PayloadEncryptResponse response = new PayloadEncryptResponse();
    response.setSenderKey(encodedPayload.getSenderKey().getKeyBytes());
    response.setCipherText(encodedPayload.getCipherText());
    response.setCipherTextNonce(encodedPayload.getCipherTextNonce().getNonceBytes());
    response.setRecipientBoxes(
        encodedPayload.getRecipientBoxes().stream()
            .map(RecipientBox::getData)
            .collect(Collectors.toList()));
    response.setRecipientNonce(encodedPayload.getRecipientNonce().getNonceBytes());
    response.setRecipientKeys(
        encodedPayload.getRecipientKeys().stream()
            .map(PublicKey::getKeyBytes)
            .collect(Collectors.toList()));
    response.setPrivacyMode(encodedPayload.getPrivacyMode().getPrivacyFlag());
    response.setAffectedContractTransactions(affectedContractTransactionMap);
    response.setExecHash(encodedPayload.getExecHash());

    return Response.ok(response).type(MIME_TYPE_JSON_2_1).build();
  }

  // path /encodedpayload/decrypt is overloaded (application/json and
  // application/vnd.tessera-2.1+json); swagger annotations cannot handle situations like this so
  // this operation documents both
  @POST
  @Path("decrypt")
  @Operation(
      summary = "/encodedpayload/decrypt",
      operationId = "decrypt",
      description =
          "decrypt an encrypted payload and return the result; does not store to the database or push to peers",
      requestBody =
          @RequestBody(
              content = {
                @Content(
                    mediaType = APPLICATION_JSON,
                    schema = @Schema(implementation = PayloadDecryptRequest.class)),
                @Content(
                    mediaType = MIME_TYPE_JSON_2_1,
                    schema = @Schema(implementation = PayloadDecryptRequest.class))
              }))
  @ApiResponse(
      responseCode = "200",
      description = "decrypted payload",
      content = {
        @Content(
            mediaType = APPLICATION_JSON,
            schema = @Schema(implementation = ReceiveResponse.class)),
        @Content(
            mediaType = MIME_TYPE_JSON_2_1,
            schema = @Schema(implementation = ReceiveResponse.class))
      })
  @Consumes(MIME_TYPE_JSON_2_1)
  @Produces(MIME_TYPE_JSON_2_1)
  public Response receive21(@Valid @NotNull final PayloadDecryptRequest request) {
    LOGGER.info("Received request to decrypt custom transaction");

    final Base64.Decoder decoder = Base64.getDecoder();
    final Map<TxHash, byte[]> affectedTxns =
        request.getAffectedContractTransactions().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> TxHash.from(decoder.decode(e.getKey())),
                    e -> decoder.decode(e.getValue())));

    final EncodedPayload requestAsPayload =
        EncodedPayload.Builder.create()
            .withSenderKey(PublicKey.from(request.getSenderKey()))
            .withCipherText(request.getCipherText())
            .withCipherTextNonce(request.getCipherTextNonce())
            .withRecipientBoxes(request.getRecipientBoxes())
            .withRecipientNonce(request.getRecipientNonce())
            .withRecipientKeys(
                request.getRecipientKeys().stream()
                    .map(PublicKey::from)
                    .collect(Collectors.toList()))
            .withPrivacyFlag(request.getPrivacyMode())
            .withAffectedContractTransactions(affectedTxns)
            .withExecHash(request.getExecHash())
            .build();

    final com.quorum.tessera.transaction.ReceiveResponse response =
        encodedPayloadManager.decrypt(requestAsPayload, null);

    final ReceiveResponse receiveResponse = new ReceiveResponse();
    receiveResponse.setPrivacyFlag(response.getPrivacyMode().getPrivacyFlag());
    receiveResponse.setPayload(response.getUnencryptedTransactionData());
    receiveResponse.setAffectedContractTransactions(
        response.getAffectedTransactions().stream()
            .map(MessageHash::getHashBytes)
            .map(Base64.getEncoder()::encodeToString)
            .toArray(String[]::new));

    Optional.ofNullable(response.getExecHash())
        .map(String::new)
        .ifPresent(receiveResponse::setExecHash);

    return Response.ok(receiveResponse).type(MIME_TYPE_JSON_2_1).build();
  }
}
