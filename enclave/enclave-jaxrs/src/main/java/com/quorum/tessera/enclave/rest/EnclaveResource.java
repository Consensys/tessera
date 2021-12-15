package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import jakarta.json.Json;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/")
public class EnclaveResource {

  private final Enclave enclave;

  private final PayloadEncoder payloadEncoder;

  public EnclaveResource(Enclave enclave) {
    this.enclave = Objects.requireNonNull(enclave);
    payloadEncoder = PayloadEncoder.create(EncodedPayloadCodec.LEGACY);
  }

  @GET
  @Path("ping")
  public Response ping() {
    Service.Status status = enclave.status();
    Status httpStatus;
    if (status == Service.Status.STARTED) {
      httpStatus = Status.OK;
    } else {
      httpStatus = Status.SERVICE_UNAVAILABLE;
    }
    return Response.status(httpStatus).entity(status.name()).build();
  }

  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @GET
  @Path("default")
  public Response defaultPublicKey() {
    final StreamingOutput streamingOutput =
        out -> out.write(enclave.defaultPublicKey().getKeyBytes());
    return Response.ok(streamingOutput).build();
  }

  @GET
  @Produces("application/json")
  @Path("forwarding")
  public Response getForwardingKeys() {

    List<String> body =
        enclave.getForwardingKeys().stream()
            .map(PublicKey::encodeToBase64)
            .collect(Collectors.toList());

    return Response.ok(
            Json.createArrayBuilder(body).build().toString(), MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  @GET
  @Produces("application/json")
  @Path("public")
  public Response getPublicKeys() {

    List<String> body =
        enclave.getPublicKeys().stream()
            .map(PublicKey::encodeToBase64)
            .collect(Collectors.toList());

    return Response.ok(
            Json.createArrayBuilder(body).build().toString(), MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  @POST
  @Path("encrypt")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response encryptPayload(EnclavePayload payload) {

    final PublicKey senderKey = PublicKey.from(payload.getSenderKey());

    final List<PublicKey> recipientPublicKeys =
        payload.getRecipientPublicKeys().stream().map(PublicKey::from).collect(Collectors.toList());

    final List<AffectedTransaction> affectedTransactions =
        convertToAffectedTransactions(payload.getAffectedContractTransactions());

    final Set<PublicKey> mandatoryRecipients =
        payload.getMandatoryRecipients().stream().map(PublicKey::from).collect(Collectors.toSet());

    final PrivacyMetadata.Builder privacyMetadataBuilder =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(payload.getPrivacyMode())
            .withAffectedTransactions(affectedTransactions)
            .withExecHash(payload.getExecHash())
            .withMandatoryRecipients(mandatoryRecipients);

    Optional.ofNullable(payload.getPrivacyGroupId())
        .map(PrivacyGroup.Id::fromBytes)
        .ifPresent(privacyMetadataBuilder::withPrivacyGroupId);

    EncodedPayload outcome =
        enclave.encryptPayload(
            payload.getData(), senderKey, recipientPublicKeys, privacyMetadataBuilder.build());

    byte[] response = payloadEncoder.encode(outcome);
    final StreamingOutput streamingOutput = out -> out.write(response);
    return Response.ok(streamingOutput).build();
  }

  @POST
  @Path("encrypt/raw")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response encryptPayload(EnclaveRawPayload enclaveRawPayload) {

    byte[] encryptedPayload = enclaveRawPayload.getEncryptedPayload();
    byte[] encryptedKey = enclaveRawPayload.getEncryptedKey();
    Nonce nonce = new Nonce(enclaveRawPayload.getNonce());
    PublicKey from = PublicKey.from(enclaveRawPayload.getFrom());

    List<PublicKey> recipientPublicKeys =
        enclaveRawPayload.getRecipientPublicKeys().stream()
            .map(PublicKey::from)
            .collect(Collectors.toList());

    RawTransaction rawTransaction = new RawTransaction(encryptedPayload, encryptedKey, nonce, from);

    final List<AffectedTransaction> affectedTransactions =
        convertToAffectedTransactions(enclaveRawPayload.getAffectedContractTransactions());

    Set<PublicKey> mandatoryRecipients =
        enclaveRawPayload.getMandatoryRecipients().stream()
            .map(PublicKey::from)
            .collect(Collectors.toSet());

    final PrivacyMetadata.Builder privacyMetaDataBuilder =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(enclaveRawPayload.getPrivacyMode())
            .withAffectedTransactions(affectedTransactions)
            .withExecHash(enclaveRawPayload.getExecHash())
            .withMandatoryRecipients(mandatoryRecipients);

    Optional.ofNullable(enclaveRawPayload.getPrivacyGroupId())
        .map(PrivacyGroup.Id::fromBytes)
        .ifPresent(privacyMetaDataBuilder::withPrivacyGroupId);

    EncodedPayload outcome =
        enclave.encryptPayload(rawTransaction, recipientPublicKeys, privacyMetaDataBuilder.build());

    byte[] response = payloadEncoder.encode(outcome);
    final StreamingOutput streamingOutput = out -> out.write(response);
    return Response.ok(streamingOutput).build();
  }

  @POST
  @Path("encrypt/toraw")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response encryptRawPayload(EnclavePayload payload) {

    RawTransaction rawTransaction =
        enclave.encryptRawPayload(payload.getData(), PublicKey.from(payload.getSenderKey()));

    EnclaveRawPayload enclaveRawPayload = new EnclaveRawPayload();
    enclaveRawPayload.setFrom(rawTransaction.getFrom().getKeyBytes());
    enclaveRawPayload.setNonce(rawTransaction.getNonce().getNonceBytes());
    enclaveRawPayload.setEncryptedPayload(rawTransaction.getEncryptedPayload());
    enclaveRawPayload.setEncryptedKey(rawTransaction.getEncryptedKey());

    return Response.ok(enclaveRawPayload).build();
  }

  @POST
  @Path("findinvalidsecurityhashes")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response findInvalidSecurityHashes(
      EnclaveFindInvalidSecurityHashesRequestPayload payload) {

    EncodedPayload encodedPayload = payloadEncoder.decode(payload.getEncodedPayload());

    List<AffectedTransaction> affectedTransactions =
        payload.getAffectedContractTransactions().stream()
            .map(
                keyValuePair ->
                    AffectedTransaction.Builder.create()
                        .withHash(keyValuePair.getKey())
                        .withPayload(payloadEncoder.decode(keyValuePair.getValue()))
                        .build())
            .collect(Collectors.toList());

    Set<TxHash> invalidSecurityHashes =
        enclave.findInvalidSecurityHashes(encodedPayload, affectedTransactions);

    EnclaveFindInvalidSecurityHashesResponsePayload responsePayload =
        new EnclaveFindInvalidSecurityHashesResponsePayload();
    responsePayload.setInvalidSecurityHashes(
        invalidSecurityHashes.stream().map(TxHash::getBytes).collect(Collectors.toList()));

    return Response.ok(responsePayload).build();
  }

  @POST
  @Path("unencrypt/raw")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response unencryptRawPayload(EnclaveRawPayload enclaveRawPayload) {

    RawTransaction rawTransaction =
        new RawTransaction(
            enclaveRawPayload.getEncryptedPayload(),
            enclaveRawPayload.getEncryptedKey(),
            new Nonce(enclaveRawPayload.getNonce()),
            PublicKey.from(enclaveRawPayload.getFrom()));

    byte[] response = enclave.unencryptRawPayload(rawTransaction);

    final StreamingOutput streamingOutput = out -> out.write(response);
    return Response.ok(streamingOutput).build();
  }

  @POST
  @Path("unencrypt")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response unencryptTransaction(EnclaveUnencryptPayload enclaveUnencryptPayload) {

    EncodedPayload payload = payloadEncoder.decode(enclaveUnencryptPayload.getData());
    PublicKey providedKey =
        Optional.ofNullable(enclaveUnencryptPayload.getProvidedKey())
            .map(PublicKey::from)
            .orElse(null);

    byte[] response = enclave.unencryptTransaction(payload, providedKey);

    final StreamingOutput streamingOutput = out -> out.write(response);
    return Response.ok(streamingOutput).build();
  }

  @POST
  @Path("addRecipient")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response createNewRecipientBox(EnclaveUnencryptPayload enclaveUnencryptPayload) {

    EncodedPayload payload = payloadEncoder.decode(enclaveUnencryptPayload.getData());
    PublicKey providedKey = PublicKey.from(enclaveUnencryptPayload.getProvidedKey());

    byte[] response = enclave.createNewRecipientBox(payload, providedKey);

    final StreamingOutput streamingOutput = out -> out.write(response);
    return Response.ok(streamingOutput).build();
  }

  private List<AffectedTransaction> convertToAffectedTransactions(
      final List<KeyValuePair> keyValuePairs) {
    return keyValuePairs.stream()
        .map(
            kvp ->
                AffectedTransaction.Builder.create()
                    .withHash(kvp.getKey())
                    .withPayload(payloadEncoder.decode(kvp.getValue()))
                    .build())
        .collect(Collectors.toUnmodifiableList());
  }
}
